package com.android.launcher3.allapps.classes;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.AppInfo;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.touch.ItemClickHandler;
import com.android.launcher3.util.Thunk;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PredictorAppAdapter extends RecyclerView.Adapter<PredictorAppAdapter.ViewHolder> {

    private static int MAX_ITEM_COUNT = 4;

    private Launcher mLauncher;

    @Thunk
    private
    ClassesCustomAppsList mApps;

    public PredictorAppAdapter(Launcher mLauncher, ClassesCustomAppsList mApps) {
        this.mLauncher = mLauncher;
        this.mApps = mApps;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_app_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.render(mApps.getPredictedApps().get(position));
    }

    @Override
    public int getItemCount() {
        return mApps.getPredictedApps().size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.recent_bubble_text)
        public BubbleTextView textView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }

        public void render(AppInfo info) {
            if (info != null) {
                textView.applyFromShortcutInfo(info.makeShortcut());
                textView.setOnClickListener(ItemClickHandler.INSTANCE);
                //textView.setOnLongClickListener(ItemLongClickListener.INSTANCE_WORKSPACE_COSTOM);
                //textView.setLongPressTimeout(ViewConfiguration.getLongPressTimeout());
            }
        }
    }


}
