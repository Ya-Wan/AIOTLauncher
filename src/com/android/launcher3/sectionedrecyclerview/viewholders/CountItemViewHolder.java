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
package com.android.launcher3.sectionedrecyclerview.viewholders;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewConfiguration;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.touch.ItemClickHandler;
import com.android.launcher3.touch.ItemLongClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by tomas on 15/07/15.
 */
public class CountItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.bubble_text)
    public BubbleTextView textView;
    @BindView(R.id.container)
    View containerView;

    private Launcher mLauncher;

    public CountItemViewHolder(View itemView, Launcher mLauncher) {
        super(itemView);
        this.mLauncher= mLauncher;
        ButterKnife.bind(this, itemView);
    }

    public void render(ShortcutInfo info){
        textView.applyFromShortcutInfo(info, false);
        textView.setOnClickListener(ItemClickHandler.INSTANCE);
        textView.setOnLongClickListener(ItemLongClickListener.INSTANCE_WORKSPACE_COSTOM);
        textView.setLongPressTimeout(ViewConfiguration.getLongPressTimeout());
    }
}
