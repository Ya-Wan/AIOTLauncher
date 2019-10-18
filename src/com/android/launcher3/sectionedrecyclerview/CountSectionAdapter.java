/*
 * Copyright (C) 2015 Tomás Ruiz-López.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.sectionedrecyclerview;

import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.AppInfo;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.entry.DefaultCategory;
import com.android.launcher3.folder.Folder;
import com.android.launcher3.sectionedrecyclerview.viewholders.CountFooterViewHolder;
import com.android.launcher3.sectionedrecyclerview.viewholders.CountHeaderViewHolder;
import com.android.launcher3.sectionedrecyclerview.viewholders.CountItemViewHolder;
import com.android.launcher3.util.ItemInfoMatcher;
import com.android.launcher3.util.LongArrayMap;
import com.android.launcher3.util.Thunk;

import java.util.ArrayList;
import java.util.List;


public class CountSectionAdapter extends SectionedRecyclerViewAdapter<CountHeaderViewHolder,
        CountItemViewHolder,
        CountFooterViewHolder> {

    protected Launcher mLauncher = null;

    private ArrayList<DefaultCategory> categories = null;
    private ArrayList<String> defaultPackages = new ArrayList<>();

    @Thunk
    private
    LongArrayMap<FolderInfo> mFolderInfos;

    public CountSectionAdapter(Launcher mLauncher, LongArrayMap<FolderInfo> mFolderInfos) {
        this.mLauncher = mLauncher;
        this.mFolderInfos = mFolderInfos;
        parseDefaultCategories();

    }

    public void setmFolderInfos(LongArrayMap<FolderInfo> mFolderInfos) {
        this.mFolderInfos = mFolderInfos;
    }

    @Override
    protected int getItemCountForSection(int section) {
        return mFolderInfos.get(section) == null ? 0 : mFolderInfos.get(section).contents.size();
    }

    @Override
    protected int getSectionCount() {
        return mFolderInfos.size();
    }

    @Override
    protected boolean hasFooterInSection(int section) {
        return false;
    }

    protected LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(mLauncher);
    }

    @Override
    protected CountHeaderViewHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.view_count_header, parent, false);
        return new CountHeaderViewHolder(view);
    }

    @Override
    protected CountFooterViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.view_count_footer, parent, false);
        return new CountFooterViewHolder(view);
    }

    @Override
    protected CountItemViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.view_count_item, parent, false);
        return new CountItemViewHolder(view, mLauncher);
    }

    @Override
    protected void onBindSectionHeaderViewHolder(CountHeaderViewHolder holder, int section) {
        FolderInfo info = mFolderInfos.get(section);
        if (info != null) {
            holder.render((String) info.title);
        }
    }

    @Override
    protected void onBindSectionFooterViewHolder(CountFooterViewHolder holder, int section) {
        //holder.render("Footer " + (section + 1));
    }

    @Override
    protected void onBindItemViewHolder(CountItemViewHolder holder, int section, int position) {
        ArrayList<ShortcutInfo> contents = mFolderInfos.get(section).contents;
        if (contents != null && contents.size() > 0) {
            holder.render(contents.get(position));
        }
    }

    public void addOrUpdateApps(List<AppInfo> apps) {
        for (AppInfo app : apps) {
            ShortcutInfo shortcutInfo = new ShortcutInfo(app);
            addToSection(shortcutInfo, findSection(shortcutInfo));
        }
    }

    private int findSection(ShortcutInfo app) {
        for (int i = 0; i < mFolderInfos.size(); i++) {
            FolderInfo folderInfo = mFolderInfos.get(i);
            if (folderInfo != null) {
                ArrayList<ShortcutInfo> contents = mFolderInfos.get(i).contents;
                for (ShortcutInfo shortcutInfo : contents) {
                    if (TextUtils.equals(app.getTargetComponent().getPackageName(),
                            shortcutInfo.getTargetComponent().getPackageName())) {
                        return i;
                    }
                }
            }

        }

        String targetPackageName = app.getTargetComponent().getPackageName();
        for (int j = 0; j < categories.size(); j++) {
            DefaultCategory defaultCategory = categories.get(j);
            if (defaultCategory.packageNames.contains(targetPackageName)) {
                return j;
            }
        }

        return mFolderInfos.size() - 1;
    }

    private void addToSection(ShortcutInfo appInfo, int section) {
        FolderInfo folderInfo = mFolderInfos.get(mFolderInfos.keyAt(section));
        folderInfo.add(appInfo, false);
        notifyDataSetChanged();

        mLauncher.getModelWriter().addOrMoveItemInDatabase(
                appInfo, folderInfo.id, 0, appInfo.cellX, appInfo.cellY);
    }

    public void removeApps(final ItemInfoMatcher matcher) {
        ArrayList<ItemInfo> items = new ArrayList<>();

        for (FolderInfo folderInfo : mFolderInfos) {
            items.addAll(folderInfo.contents);
        }

        for (ItemInfo itemToRemove : matcher.filterItemInfos(items)) {
            for (FolderInfo folderInfo : mFolderInfos) {
                folderInfo.remove((ShortcutInfo) itemToRemove, false);
            }
        }

        notifyDataSetChanged();

    }

    private void parseDefaultCategories() {
        XmlResourceParser parser = mLauncher.getResources().getXml(R.xml.default_hotseat_category);
        final int depth = parser.getDepth();

        DefaultCategory defaultCategory= null;
        try {
            int type = parser.getEventType();
            while (type != XmlResourceParser.END_DOCUMENT) {
                switch (type) {
                    case XmlResourceParser.START_DOCUMENT:
                        categories = new ArrayList<>();
                        defaultPackages.clear();
                        break;

                    case XmlResourceParser.START_TAG:
                        if (parser.getName().equals("category")) {
                            defaultCategory = new DefaultCategory();
                            defaultCategory.setCategoryName(parser.getAttributeValue(0));
                        }

                        if (parser.getName().equals("packageName")) {
                            String packageName = parser.nextText();
                            defaultCategory.addPackage(packageName);
                            defaultPackages.add(packageName);
                        }
                        break;

                    case XmlResourceParser.END_TAG:
                        if (parser.getName().equals("category")) {
                            categories.add(defaultCategory);
                        }
                        break;
                }
                type = parser.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
