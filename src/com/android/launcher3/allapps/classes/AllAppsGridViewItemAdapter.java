package com.android.launcher3.allapps.classes;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.touch.ItemClickHandler;
import com.android.launcher3.touch.ItemLongClickListener;

import java.util.ArrayList;

public class AllAppsGridViewItemAdapter extends BaseAdapter {

    private Launcher mLauncher;
    private LayoutInflater mLayoutInflater;
    private ArrayList<ShortcutInfo> infos;

    public AllAppsGridViewItemAdapter(Launcher mLauncher, ArrayList<ShortcutInfo> shortcutInfos) {
        this.mLauncher = mLauncher;
        mLayoutInflater = LayoutInflater.from(mLauncher);
        this.infos = shortcutInfos;
    }

    @Override
    public int getCount() {
        return infos.size();
    }

    @Override
    public ShortcutInfo getItem(int position) {
        return infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        BubbleTextView icon = (BubbleTextView) mLayoutInflater.inflate(R.layout.custom_all_apps_icon, parent, false);
        ShortcutInfo info = infos.get(position);
        icon.setTag(info);
        render(icon, info);

        return icon;
    }

    public void render(BubbleTextView icon, ShortcutInfo info) {
        icon.applyFromShortcutInfo(info, false);
        icon.setOnClickListener(ItemClickHandler.INSTANCE);
        icon.setOnLongClickListener(ItemLongClickListener.INSTANCE_WORKSPACE_COSTOM);
        icon.setLongPressTimeout(ViewConfiguration.getLongPressTimeout());
    }

    public class GridItemViewHolder extends RecyclerView.ViewHolder {

        BubbleTextView icon;

        private Launcher mLauncher;

        public GridItemViewHolder(View itemView, Launcher mLauncher) {
            super(itemView);
            this.mLauncher = mLauncher;
            icon = (BubbleTextView) itemView;
        }

        public void render(ShortcutInfo info) {
            icon.applyFromShortcutInfo(info, false);
            icon.setOnClickListener(ItemClickHandler.INSTANCE);
            icon.setOnLongClickListener(ItemLongClickListener.INSTANCE_WORKSPACE_COSTOM);
            icon.setLongPressTimeout(ViewConfiguration.getLongPressTimeout());
        }
    }
}
