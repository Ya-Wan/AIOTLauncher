package com.android.launcher3.allapps.classes;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.launcher3.FolderInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.Thunk;
import com.android.launcher3.widget.AppListGridView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AllAppsCustomGridAdapter extends RecyclerView.Adapter<AllAppsCustomGridAdapter.GridViewHolder> {

    private Launcher mLauncher;
    private LayoutInflater mLayoutInflater;

    @Thunk
    private
    ClassesCustomAppsList mApps;

    public AllAppsCustomGridAdapter(Launcher launcher, ClassesCustomAppsList mApps) {
        this.mLauncher = launcher;
        mLayoutInflater = LayoutInflater.from(launcher);
        this.mApps = mApps;
    }

    @Override
    public AllAppsCustomGridAdapter.GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.custom_all_apps_gridview_item, parent, false);

        return new GridViewHolder(view, mLauncher);
    }

    @Override
    public void onBindViewHolder(AllAppsCustomGridAdapter.GridViewHolder holder, int position) {
        holder.itemView.setBackground(Utilities.isDarkTheme(mLauncher) ? mLauncher.getDrawable(R.drawable.allapps_classes_bg_dark)
                : mLauncher.getDrawable(R.drawable.allapps_classes_bg));

        FolderInfo folderInfo = mApps.getClassesInfos().get(position);
        holder.title.setText(folderInfo.title);

        if (folderInfo.contents != null) {
            AllAppsGridViewItemAdapter adapter = new AllAppsGridViewItemAdapter(mLauncher, folderInfo.contents);
            holder.gridView.setAdapter(adapter);
        }

    }

    @Override
    public int getItemCount() {
        return mApps.getClassesInfos() == null ? 0 : mApps.getClassesInfos().size();
    }


    public class GridViewHolder extends RecyclerView.ViewHolder {

        public View mContent;
        @BindView(R.id.class_title)
        TextView title;
        @BindView(R.id.class_app_view)
        AppListGridView gridView;

        private Launcher mLauncher;

        public GridViewHolder(View itemView, Launcher mLauncher) {
            super(itemView);
            mContent = itemView;
            this.mLauncher = mLauncher;
            ButterKnife.bind(this, itemView);
        }
    }
}
