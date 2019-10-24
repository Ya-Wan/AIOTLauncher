package com.android.launcher3.allapps.classes;

import android.content.Context;
import android.util.Log;

import com.android.launcher3.AppInfo;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.allapps.AllAppsStore;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.ComponentKeyMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ClassesCustomAppsList implements AllAppsStore.OnUpdateListener {
    public static final String TAG = "ClassesCustomAppsList";

    private Launcher mLauncher;

    private final List<AppInfo> mApps = new ArrayList<>();
    //
    private final List<FolderInfo> classesInfos = new ArrayList<>();
    // The set of predicted app component names
    private final List<ComponentKeyMapper<AppInfo>> mPredictedAppComponents = new ArrayList<>();
    // The set of predicted apps resolved from the component names and the current set of apps
    private final List<AppInfo> mPredictedApps = new ArrayList<>();
    private final HashMap<ComponentKey, AppInfo> mComponentToAppMap = new HashMap<>();

    private AllAppsCustomGridAdapter mAdapter;
    private PredictorAppAdapter mPredictorAppAdapter;

    private String[] titles;

    public ClassesCustomAppsList(Context context) {
        mLauncher = (Launcher) context;
        titles = new String[]{
                mLauncher.getResources().getString(R.string.education_folder_name),
                mLauncher.getResources().getString(R.string.work_folder_name),
                mLauncher.getResources().getString(R.string.entertainment_folder_name),
                mLauncher.getResources().getString(R.string.life_folder_name),
                mLauncher.getResources().getString(R.string.all_app_folder_name),
        };

        initClassesInfos();
    }

    private void initClassesInfos() {
        classesInfos.clear();
        for (int i = 0; i < titles.length; i++) {
            FolderInfo folderInfo = new FolderInfo();
            folderInfo.title = titles[i];
            folderInfo.id = i+1;
            classesInfos.add(folderInfo);
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

    @Override
    public void onAppsUpdated() {

    }
}
