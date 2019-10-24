package com.android.launcher3.allapps.classes;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;

import java.util.ArrayList;

public class AllAppsMoveAdapter extends RecyclerView.Adapter<AllAppsMoveAdapter.ItemViewHolder> {

    private Context context;

    private ArrayList<MoveInfo> items = new ArrayList<>();

    private ShortcutInfo shortcutInfo;

    public AllAppsMoveAdapter(Context context) {
        this.context = context;
    }

    public ArrayList<MoveInfo> setItems(ShortcutInfo info) {
        items.clear();
        Resources resources = context.getResources();
        this.shortcutInfo = info;

        items.add(new MoveInfo(0, resources.getString(R.string.education_folder_name)));
        items.add(new MoveInfo(1, resources.getString(R.string.work_folder_name)));
        items.add(new MoveInfo(2, resources.getString(R.string.entertainment_folder_name)));
        items.add(new MoveInfo(3, resources.getString(R.string.life_folder_name)));
        items.add(new MoveInfo(4, resources.getString(R.string.all_app_folder_name)));

        items.remove((int) info.container - 1);

        return items;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_apps_move_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.render(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.title);
        }

        public void render(MoveInfo info) {
            textView.setText(info.title);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Launcher) context).notifyUpdate(shortcutInfo, info.index);
                }
            });
        }
    }
}
