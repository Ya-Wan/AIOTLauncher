package com.android.launcher3.sectionedrecyclerview;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.android.launcher3.AppInfo;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.touch.ItemClickHandler;
import com.android.launcher3.touch.ItemLongClickListener;
import com.android.launcher3.util.ItemInfoMatcher;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecentAppAdapter extends RecyclerView.Adapter<RecentAppAdapter.ViewHolder> {

    private static int MAX_ITEM_COUNT = 4;
    private ArrayList<ShortcutInfo> infos;
    private Launcher mLauncher;

    public RecentAppAdapter(Launcher mLauncher, ArrayList<ShortcutInfo> infos) {
        this.infos = infos;
        this.mLauncher = mLauncher;

        Log.d("RecentAppAdapter", "RecentAppAdapter: " + infos.size());
    }

    public void setInfos(ArrayList<ShortcutInfo> infos) {
        this.infos = infos;
    }

    public void removeRecentApp(final ItemInfoMatcher matcher) {

        ArrayList<ItemInfo> items = new ArrayList<>(infos);

        for (ItemInfo itemToRemove : matcher.filterItemInfos(items)) {
            infos.remove((ShortcutInfo) itemToRemove);
        }

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_app_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.render(infos.get(position));
    }

    @Override
    public int getItemCount() {
        return infos.size() > MAX_ITEM_COUNT ? MAX_ITEM_COUNT : infos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.recent_bubble_text)
        public BubbleTextView textView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }

        public void render(ShortcutInfo info) {
            if (info != null) {
                Log.d("y.wan", "render: " + info.toString());
                textView.applyFromShortcutInfo(info);
                textView.setOnClickListener(ItemClickHandler.INSTANCE);
                textView.setOnLongClickListener(ItemLongClickListener.INSTANCE_WORKSPACE_COSTOM);
                textView.setLongPressTimeout(ViewConfiguration.getLongPressTimeout());
            }
        }

        public String getAppName(Context context, String pkgName) {

            try {

                PackageManager packageManager = context.getPackageManager();

                PackageInfo packageInfo = packageManager.getPackageInfo(

                        pkgName, 0);

                int labelRes = packageInfo.applicationInfo.labelRes;

                return context.getResources().getString(labelRes);

            } catch (Exception e) {

                e.printStackTrace();

            }

            return null;

        }
    }


}
