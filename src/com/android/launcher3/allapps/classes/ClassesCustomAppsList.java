package com.android.launcher3.allapps.classes;

import android.content.Context;
import android.util.Log;

import com.android.launcher3.AppInfo;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.allapps.AllAppsStore;
import com.android.launcher3.allapps.AppInfoComparator;
import com.android.launcher3.compat.AlphabeticIndexCompat;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.ComponentKeyMapper;
import com.android.launcher3.util.LabelComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ClassesCustomAppsList {
    public static final String TAG = "ClassesCustomAppsList";

    private Launcher mLauncher;

    private final ArrayList<ShortcutInfo> mApps = new ArrayList<>();
    //
    private final List<FolderInfo> classesInfos = new ArrayList<>();
    // The set of predicted app component names
    private final List<ComponentKeyMapper<AppInfo>> mPredictedAppComponents = new ArrayList<>();
    // The set of predicted apps resolved from the component names and the current set of apps
    private final List<AppInfo> mPredictedApps = new ArrayList<>();
    private final HashMap<ComponentKey, AppInfo> mComponentToAppMap = new HashMap<>();
    private ShortcutInfoComparator mAppNameComparator;
    private HashMap<CharSequence, String> mCachedSectionNames = new HashMap<>();
    private AlphabeticIndexCompat mIndexer;

    String[] filterPkgNames = new String[] {
            "com.coocaa.app_browser",
            "com.tianci.user",
            "com.skyworth.skywebviewapp.webview",
            "com.baidu.duer.tv.video"
    };

    final HashMap<Integer, List<ShortcutInfo>> mClassesContents = new HashMap<>();

    private ArrayList<String> intentList = new ArrayList<>();

    private AllAppsCustomGridAdapter mAdapter;
    private PredictorAppAdapter mPredictorAppAdapter;

    private String[] titles;

    public ClassesCustomAppsList(Context context) {
        mLauncher = (Launcher) context;
        titles = new String[]{
                mLauncher.getResources().getString(R.string.entertainment_folder_name),
                mLauncher.getResources().getString(R.string.education_folder_name),
                mLauncher.getResources().getString(R.string.shopping_folder_name),
                mLauncher.getResources().getString(R.string.news_folder_name),
                mLauncher.getResources().getString(R.string.all_app_folder_name),
        };

        mAppNameComparator = new ShortcutInfoComparator(context);
        mIndexer = new AlphabeticIndexCompat(context);
        initClassesInfos();
    }

    private void initClassesInfos() {
        classesInfos.clear();
        //mClassesContents.clear();

        for (int i = 0; i < titles.length; i++) {
            FolderInfo folderInfo = new FolderInfo();
            folderInfo.title = titles[i];
            classesInfos.add(folderInfo);
            mClassesContents.put(i, folderInfo.contents);
        }
    }

    public void setClassesApps(int index,long id, ArrayList<ShortcutInfo> infos) {
        FolderInfo folderInfo = classesInfos.get(index);
        folderInfo.id = id;

        for (ShortcutInfo info : infos) {
            info.screenId = index;
            folderInfo.add(info, false);
            intentList.add(info.getIntent().toUri(0));
        }

    }

    public long onContentsAdd(int index, ShortcutInfo info) {
        FolderInfo folderInfo = classesInfos.get(index);

        info.screenId = index;
        folderInfo.add(info, false);

        intentList.add(info.getIntent().toUri(0));

        return index;

    }

    public void onContentsRemove(int index, ShortcutInfo info) {
        FolderInfo folderInfo = classesInfos.get(index);

        if (folderInfo != null) {
            folderInfo.remove(info, false);

            intentList.remove(info.getIntent().toUri(0));

        }

    }

    public List<FolderInfo> getClassesInfos() {
        return classesInfos;
    }

    /**
     * Returns the predicted apps.
     */
    public List<AppInfo> getPredictedApps() {
        return mPredictedApps;
    }

    public void setAdapter(AllAppsCustomGridAdapter adapter) {
        mAdapter = adapter;
    }

    public void setPredictorAppAdapter(PredictorAppAdapter adapter) {
        mPredictorAppAdapter = adapter;
    }

    public void addApps(List<AppInfo> apps) {

    }

    public void updateApps(List<AppInfo> apps) {

    }

    public void removeApps(List<AppInfo> apps) {

    }

    /**
     * Sets the current set of predicted apps.
     * <p>
     * This can be called before we get the full set of applications, we should merge the results
     * only in onAppsUpdated() which is idempotent.
     * <p>
     * If the number of predicted apps is the same as the previous list of predicted apps,
     * we can optimize by swapping them in place.
     */
    public void setPredictedApps(List<ComponentKeyMapper<AppInfo>> apps, AllAppsStore allAppsStore) {
        mPredictedAppComponents.clear();
        mPredictedAppComponents.addAll(apps);

        List<AppInfo> newPredictedApps = processPredictedAppComponents(apps, allAppsStore);
        // We only need to do work if any of the visible predicted apps have changed.
        if (!newPredictedApps.equals(mPredictedApps)) {
            if (newPredictedApps.size() == mPredictedApps.size()) {
                swapInNewPredictedApps(newPredictedApps);
            } else {
                // We need to update the appIndex of all the items.
                //onAppsUpdated();
                swapInNewPredictedApps(newPredictedApps);
            }
        }
    }

    /**
     * Swaps out the old predicted apps with the new predicted apps, in place. This optimization
     * allows us to skip an entire relayout that would otherwise be called by notifyDataSetChanged.
     * <p>
     * Note: This should only be called if the # of predicted apps is the same.
     * This method assumes that predicted apps are the first items in the adapter.
     */
    private void swapInNewPredictedApps(List<AppInfo> apps) {
        mPredictedApps.clear();
        mPredictedApps.addAll(apps);
    }

    private List<AppInfo> processPredictedAppComponents(List<ComponentKeyMapper<AppInfo>> components, AllAppsStore allAppsStore) {
        if (allAppsStore.getComponentToAppMap().isEmpty()) {
            // Apps have not been bound yet.
            return Collections.emptyList();
        }

        List<AppInfo> predictedApps = new ArrayList<>();
        for (ComponentKeyMapper<AppInfo> mapper : components) {
            AppInfo info = mapper.getItem(allAppsStore.getComponentToAppMap());
            if (info != null) {
                predictedApps.add(info);
            } else {
                if (FeatureFlags.IS_DOGFOOD_BUILD) {
                    Log.e(TAG, "Predicted app not found: " + mapper);
                }
            }
            // Stop at the number of predicted apps
            if (predictedApps.size() == 4) {
                break;
            }
        }
        return predictedApps;
    }

    /**
     * Returns all the apps.
     */
    public ArrayList<ShortcutInfo> getApps() {
        return mApps;
    }

    private boolean filterApp(String pkgName) {

        List<String> packagesNames = Arrays.asList(filterPkgNames);
        if (packagesNames.contains(pkgName)) {
            return true;
        }

        return false;
    }

    public void onAppsUpdated() {
        mApps.clear();

        for (AppInfo app : mLauncher.getAppsView().getAppsStore().getApps()) {
            if (!filterApp(app.getTargetComponent().getPackageName())) {
                mApps.add(app.makeShortcut());
            }
        }

        Collections.sort(mApps, mAppNameComparator);

        // As a special case for some languages (currently only Simplified Chinese), we may need to
        // coalesce sections
        Locale curLocale = mLauncher.getResources().getConfiguration().locale;
        boolean localeRequiresSectionSorting = curLocale.equals(Locale.SIMPLIFIED_CHINESE);
        if (localeRequiresSectionSorting) {
            // Compute the section headers. We use a TreeMap with the section name comparator to
            // ensure that the sections are ordered when we iterate over it later
            TreeMap<String, ArrayList<ShortcutInfo>> sectionMap = new TreeMap<>(new LabelComparator());
            for (ShortcutInfo info : mApps) {
                // Add the section to the cache
                String sectionName = getAndUpdateCachedSectionName(info.title);

                // Add it to the mapping
                ArrayList<ShortcutInfo> sectionApps = sectionMap.get(sectionName);
                if (sectionApps == null) {
                    sectionApps = new ArrayList<>();
                    sectionMap.put(sectionName, sectionApps);
                }
                sectionApps.add(info);
            }

            // Add each of the section apps to the list in order
            mApps.clear();
            for (Map.Entry<String, ArrayList<ShortcutInfo>> entry : sectionMap.entrySet()) {
                mApps.addAll(entry.getValue());
            }
        } else {
            // Just compute the section headers for use below
            for (ShortcutInfo info : mApps) {
                // Add the section to the cache
                getAndUpdateCachedSectionName(info.title);
            }
        }
    }

    /**
     * Returns the cached section name for the given title, recomputing and updating the cache if
     * the title has no cached section name.
     */
    private String getAndUpdateCachedSectionName(CharSequence title) {
        String sectionName = mCachedSectionNames.get(title);
        if (sectionName == null) {
            sectionName = mIndexer.computeSectionName(title);
            mCachedSectionNames.put(title, sectionName);
        }
        return sectionName;
    }
}
