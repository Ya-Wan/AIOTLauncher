/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.allapps.classes;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Process;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.launcher3.AppInfo;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsStore;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import com.android.launcher3.entry.DefaultCategory;
import com.android.launcher3.util.ComponentKeyMapper;
import com.android.launcher3.util.ItemInfoMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * The all apps view container.
 */
public class AllAppsCustomContainerView extends LinearLayout {

    private final Launcher mLauncher;
    private final ItemInfoMatcher mPersonalMatcher = ItemInfoMatcher.ofUser(Process.myUserHandle());
    private final ItemInfoMatcher mWorkMatcher = ItemInfoMatcher.not(mPersonalMatcher);

    private FrameLayout predictAppContainer;

    private RecyclerView classesRecycleView;
    private RecyclerView predictRecycleView;

    private AllAppsCustomGridAdapter mAdapter;
    private PredictorAppAdapter mPredictorAppAdapter;
    private ClassesCustomAppsList mApps;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.LayoutManager mPredictLayoutManager;

    private ArrayList<DefaultCategory> categories = null;
    private ArrayList<String> defaultPackages = new ArrayList<>();


    public AllAppsCustomContainerView(Context context) {
        this(context, null);
    }

    public AllAppsCustomContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsCustomContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mLauncher = Launcher.getLauncher(context);
        mApps = new ClassesCustomAppsList(mLauncher);
        mAdapter = new AllAppsCustomGridAdapter(mLauncher, mApps);
        mApps.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(mLauncher);

        mPredictorAppAdapter = new PredictorAppAdapter(mLauncher, mApps);
        mApps.setPredictorAppAdapter(mPredictorAppAdapter);
        mPredictLayoutManager = new GridLayoutManager(mLauncher, 4);

        parseDefaultCategories();
    }

    public void onClassesUpdate(int index, long id, ArrayList<ShortcutInfo> shortcutInfos) {
        FolderInfo folderInfo = mApps.getClassesInfos().get(index);
        if (folderInfo != null) {
            folderInfo.id =  id;
            for (ShortcutInfo shortcutInfo : shortcutInfos) {
                folderInfo.add(shortcutInfo, false);
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    public void addOrUpdateApps(List<AppInfo> apps) {
        for (AppInfo app : apps) {
            addToIndex(app, getIndex(app));
        }
    }

    public void removeApps(List<AppInfo> apps) {
        List<FolderInfo> mFolderInfos = mApps.getClassesInfos();

        for (AppInfo app : apps) {
            FolderInfo folderInfo = mFolderInfos.get((int) app.container);
            if (folderInfo != null) {
                folderInfo.remove(app.makeShortcut(), false);
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    public void removeApps(final ItemInfoMatcher matcher) {
        ArrayList<ItemInfo> items = new ArrayList<>();
        List<FolderInfo> mFolderInfos = mApps.getClassesInfos();

        for (FolderInfo folderInfo : mFolderInfos) {
            items.addAll(folderInfo.contents);
        }

        for (ItemInfo itemToRemove : matcher.filterItemInfos(items)) {
            for (FolderInfo folderInfo : mFolderInfos) {
                folderInfo.remove((ShortcutInfo) itemToRemove, false);
            }
        }

        mAdapter.notifyDataSetChanged();

    }

    private int getIndex(AppInfo app) {
        List<FolderInfo> mFolderInfos = mApps.getClassesInfos();
        String targetPackageName = app.getTargetComponent().getPackageName();

        for (int i = 0; i < mFolderInfos.size(); i++) {
            FolderInfo folderInfo = mFolderInfos.get(i);
            if (folderInfo != null) {
                ArrayList<ShortcutInfo> contents = mFolderInfos.get(i).contents;
                for (ShortcutInfo shortcutInfo : contents) {
                    if (TextUtils.equals(targetPackageName,
                            shortcutInfo.getTargetComponent().getPackageName())) {
                        return i;
                    }
                }
            }

        }


        for (int j = 0; j < categories.size(); j++) {
            DefaultCategory defaultCategory = categories.get(j);
            if (defaultCategory.packageNames.contains(targetPackageName)) {
                return j;
            }
        }

        return mApps.getClassesInfos().size() - 1;
    }

    public void moveInfoToIndex(ShortcutInfo info, int toIndex) {
        List<FolderInfo> infos = mApps.getClassesInfos();

        int fromIndex = 0;
        for (int i = 0; i < infos.size(); i++) {
            FolderInfo folderInfo = infos.get(i);
            if (folderInfo.contents.contains(info)) {
                fromIndex = i;
                break;
            }
        }
        
        FolderInfo toFolderInfo = infos.get(toIndex);
        if (toFolderInfo != null) {
            toFolderInfo.add(info, false);
        }

        FolderInfo fromFolderInfo = mApps.getClassesInfos().get(fromIndex);
        if (fromFolderInfo != null) {
            fromFolderInfo.remove(info, false);
        }

        mLauncher.getModelWriter().addOrMoveItemInDatabase(
                info, toFolderInfo.id, 0, info.cellX, info.cellY);

        mAdapter.notifyDataSetChanged();
    }

    private void addToIndex(AppInfo appInfo, int index) {
        FolderInfo folderInfo = mApps.getClassesInfos().get(index);
        if (folderInfo != null) {
            folderInfo.add(appInfo.makeShortcut(), false);
        }

        mAdapter.notifyDataSetChanged();

        mLauncher.getModelWriter().addOrMoveItemInDatabase(
                appInfo, folderInfo.id, 0, appInfo.cellX, appInfo.cellY);
    }

    private void removeFromIndex(AppInfo appInfo, int index) {
        FolderInfo folderInfo = mApps.getClassesInfos().get(index);
        if (folderInfo != null) {
            folderInfo.remove(appInfo.makeShortcut(), false);
        }

        mAdapter.notifyDataSetChanged();

        mLauncher.getModelWriter().addOrMoveItemInDatabase(
                appInfo, folderInfo.id, 0, appInfo.cellX, appInfo.cellY);
    }

    private void onAppsUpdated() {

    }

    /**
     * Sets the current set of predicted apps.
     */
    public void setPredictedApps(List<ComponentKeyMapper<AppInfo>> apps, AllAppsStore allAppsStore) {
        mApps.setPredictedApps(apps, allAppsStore);
        mPredictorAppAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        predictAppContainer = findViewById(R.id.predict_app_container);
        classesRecycleView = findViewById(R.id.all_apps_classes_view);
        predictRecycleView = findViewById(R.id.predict_app_view);

        classesRecycleView.setLayoutManager(mLayoutManager);
        classesRecycleView.setAdapter(mAdapter);

        predictRecycleView.setLayoutManager(mPredictLayoutManager);
        predictRecycleView.setAdapter(mPredictorAppAdapter);

    }

    public void updateAllAppsContainerTheme() {
        predictAppContainer.setBackground(Utilities.isDarkTheme(mLauncher) ? mLauncher.getDrawable(R.drawable.allapps_classes_bg_dark) :
                mLauncher.getDrawable(R.drawable.allapps_classes_bg));
        for (int i = 0; i < mAdapter.getItemCount(); i ++) {
            mAdapter.notifyItemChanged(i);
        }
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
