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
import com.android.launcher3.entry.DefaultCategory;
import com.android.launcher3.util.ComponentKeyMapper;
import com.android.launcher3.util.ItemInfoMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The all apps view container.
 */
public class AllAppsCustomContainerView extends LinearLayout {

    private static final String TAG = "AllAppsCustomView";

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
        mLauncher.getAppsView().getAppsStore().addUpdateListener(this::onAppsUpdated);
        parseDefaultCategories();
    }

    /**
     * bind item to category
     */
    public void setClassesApps(int index, long id, ArrayList<ShortcutInfo> shortcutInfos) {
        mApps.setClassesApps(index, id, shortcutInfos);

        mAdapter.notifyDataSetChanged();
    }

    public void addOrUpdateApps(List<AppInfo> apps) {
        for (AppInfo app : apps) {
            addToIndex(app, getIndex(app));
        }
    }

    public void updateShortcuts(ArrayList<ShortcutInfo> shortcuts) {
        for (ShortcutInfo shortcutInfo : shortcuts) {
            updateShortcuts(shortcutInfo);
        }
    }

    public void updateShortcuts(ShortcutInfo shortcut) {
        addToIndex(shortcut, getIndex(shortcut));
    }

    /**
     * add item to specify category
     */
    private void addToIndex(ItemInfo item, int index) {
        int count = mAdapter.getItemCount();
        long folderInfoId = count - 1;

        if (index == count - 1) {
            notifyUpdateAllApps();
            return;
        }

        if (item instanceof AppInfo) {
            folderInfoId = mApps.onContentsAdd(index, ((AppInfo) item).makeShortcut());
        } else if (item instanceof ShortcutInfo) {
            folderInfoId = mApps.onContentsAdd(index, (ShortcutInfo) item);
        }

        Log.d(TAG, "addToIndex: " + folderInfoId);

        mAdapter.notifyItemChanged(index);

        mLauncher.getModelWriter().addOrMoveItemInDatabase(
                item, folderInfoId, 0, item.cellX, item.cellY);
    }

    /**
     * get item position by pkgName
     *
     * @return index
     */
    private int getIndex(ItemInfo item) {

        String targetPackageName = item.getTargetComponent().getPackageName();
        Log.d(TAG, "getIndex targetPackageName: " + targetPackageName);

        /*for (Map.Entry<Integer, List<ShortcutInfo>> entry : mApps.mClassesContents.entrySet()) {
            List<ShortcutInfo> shortcutInfos = entry.getValue();
            for (ShortcutInfo shortcutInfo : shortcutInfos) {
                if (TextUtils.equals(targetPackageName,
                        shortcutInfo.getTargetComponent().getPackageName())) {
                    return entry.getKey();
                }
            }
        }*/

        for (int j = 0; j < categories.size(); j++) {
            DefaultCategory defaultCategory = categories.get(j);
            if (defaultCategory.packageNames.contains(targetPackageName)) {
                return j;
            }
        }

        return mApps.mClassesContents.size() - 1;
    }

    /**
     * Removes items that match the {@param matcher}. When applications are removed
     * as a part of an update, this is called to ensure application
     * shortcuts are not removed.
     **/
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

    /**
     * move item to specify category
     *
     * @param info    item
     * @param toIndex category position
     */
    public void moveInfoToIndex(ShortcutInfo info, int toIndex) {

        FolderInfo toFolderInfo = mApps.getClassesInfos().get(toIndex);

        mApps.onContentsRemove((int) info.screenId, info);
        mAdapter.notifyItemChanged((int) info.screenId);

        mApps.onContentsAdd(toIndex, info);
        if (toIndex == mAdapter.getItemCount() - 1) {
            notifyUpdateAllApps();
        } else {
            mAdapter.notifyItemChanged(toIndex);
        }

        if (toIndex == mApps.getClassesInfos().size()) {
            mLauncher.getModelWriter().deleteItemFromDatabase(info);
        }

        mLauncher.getModelWriter().addOrMoveItemInDatabase(
                info, toFolderInfo.id, toIndex, info.cellX, info.cellY);

    }

    public void onAppsUpdated() {
        mApps.onAppsUpdated();
        notifyUpdateAllApps();
    }

    /**
     * update all apps category,items do not contains other category items
     */
    private void notifyUpdateAllApps() {
        int itemCount = mAdapter.getItemCount();
        ArrayList<ShortcutInfo> apps = mApps.getApps();
        ArrayList<ShortcutInfo> shortcutInfos = new ArrayList<>(apps);
        List<FolderInfo> infos = mApps.getClassesInfos();

        for (int i = 0; i < apps.size(); i++) {
            ShortcutInfo info = apps.get(i);
            String targetPackageName = info.getTargetComponent().getPackageName();
            Log.d(TAG, "notifyUpdate: " + targetPackageName);
            for (int j = 0; j < itemCount - 1; j++) {
                FolderInfo folderInfo = infos.get(j);
                for (int k = 0; k < folderInfo.contents.size(); k++) {
                    ShortcutInfo shortcutInfo = folderInfo.contents.get(k);
                    if (TextUtils.equals(targetPackageName,
                            shortcutInfo.getTargetComponent().getPackageName())) {
                        shortcutInfos.remove(info);
                    }
                }
            }
        }

        FolderInfo folderInfo = infos.get(itemCount - 1);
        folderInfo.contents.clear();
        for (ShortcutInfo shortcutInfo : shortcutInfos) {
            shortcutInfo.screenId = itemCount - 1;
            folderInfo.add(shortcutInfo, false);
        }
        mAdapter.notifyItemChanged(itemCount - 1);
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
        predictAppContainer.setBackground(Utilities.isDarkTheme(mLauncher) ? mLauncher.getDrawable(R.drawable.predict_app_container_bg_dark) :
                mLauncher.getDrawable(R.drawable.predict_app_container_bg));
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            mAdapter.notifyItemChanged(i);
        }
    }

    private void parseDefaultCategories() {
        XmlResourceParser parser = mLauncher.getResources().getXml(R.xml.default_hotseat_category);
        final int depth = parser.getDepth();

        DefaultCategory defaultCategory = null;
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
