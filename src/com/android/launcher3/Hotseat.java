/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.launcher3;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.entry.HotseatCategory;
import com.android.launcher3.logging.UserEventDispatcher.LogContainerProvider;
import com.android.launcher3.userevent.nano.LauncherLogProto.Action;
import com.android.launcher3.userevent.nano.LauncherLogProto.ContainerType;
import com.android.launcher3.userevent.nano.LauncherLogProto.ControlType;
import com.android.launcher3.userevent.nano.LauncherLogProto.Target;
import com.android.launcher3.util.ComponentKey;

import java.util.ArrayList;
import java.util.HashMap;

import static com.android.launcher3.LauncherState.ALL_APPS;

public class Hotseat extends FrameLayout implements LogContainerProvider, Insettable {

    private final Launcher mLauncher;
    private CellLayout mContent;

    @ViewDebug.ExportedProperty(category = "launcher")
    private boolean mHasVerticalHotseat;

    private ArrayList<HotseatCategory> categories = null;
    private ArrayList<String> defaultPackages = new ArrayList<>();

    private final HashMap<Integer, HashMap<ComponentKey, AppInfo>> mComponentToAppMap = new HashMap<>();

    public Hotseat(Context context) {
        this(context, null);
    }

    public Hotseat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLauncher = Launcher.getLauncher(context);
        parseDefaultCategories();
    }

    public CellLayout getLayout() {
        return mContent;
    }

    /* Get the orientation invariant order of the item in the hotseat for persistence. */
    int getOrderInHotseat(int x, int y) {
        return mHasVerticalHotseat ? (mContent.getCountY() - y - 1) : x;
    }

    /* Get the orientation specific coordinates given an invariant order in the hotseat. */
    int getCellXFromOrder(int rank) {
        return mHasVerticalHotseat ? 0 : rank;
    }

    int getCellYFromOrder(int rank) {
        return mHasVerticalHotseat ? (mContent.getCountY() - (rank + 1)) : 0;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContent = findViewById(R.id.layout);
    }

    void resetLayout(boolean hasVerticalHotseat) {
        mContent.removeAllViewsInLayout();
        mHasVerticalHotseat = hasVerticalHotseat;
        InvariantDeviceProfile idp = mLauncher.getDeviceProfile().inv;
        if (hasVerticalHotseat) {
            mContent.setGridSize(1, idp.numHotseatIcons);
        } else {
            mContent.setGridSize(idp.numHotseatIcons, 1);
        }

        if (!FeatureFlags.NO_ALL_APPS_ICON) {
            // Add the Apps button
            Context context = getContext();
            DeviceProfile grid = mLauncher.getDeviceProfile();
            int allAppsButtonRank = grid.inv.getAllAppsButtonRank();

            LayoutInflater inflater = LayoutInflater.from(context);
            TextView allAppsButton = (TextView)
                    inflater.inflate(R.layout.all_apps_button, mContent, false);
            Drawable d = context.getResources().getDrawable(R.drawable.all_apps_button_icon);
            d.setBounds(0, 0, grid.iconSizePx, grid.iconSizePx);

            int scaleDownPx = getResources().getDimensionPixelSize(R.dimen.all_apps_button_scale_down);
            Rect bounds = d.getBounds();
            d.setBounds(bounds.left, bounds.top + scaleDownPx / 2, bounds.right - scaleDownPx,
                    bounds.bottom - scaleDownPx / 2);
            allAppsButton.setCompoundDrawables(null, d, null, null);

            allAppsButton.setContentDescription(context.getString(R.string.all_apps_button_label));
            if (mLauncher != null) {
                allAppsButton.setOnClickListener((v) -> {
                    if (!mLauncher.isInState(ALL_APPS)) {
                        mLauncher.getUserEventDispatcher().logActionOnControl(Action.Touch.TAP,
                                ControlType.ALL_APPS_BUTTON);
                        mLauncher.getStateManager().goToState(ALL_APPS);
                    }
                });
                allAppsButton.setOnFocusChangeListener(mLauncher.mFocusHandler);
            }

            // Note: We do this to ensure that the hotseat is always laid out in the orientation of
            // the hotseat in order regardless of which orientation they were added
            int x = getCellXFromOrder(allAppsButtonRank);
            int y = getCellYFromOrder(allAppsButtonRank);
            CellLayout.LayoutParams lp = new CellLayout.LayoutParams(x, y, 1, 1);
            lp.canReorder = false;
            mContent.addViewToCellLayout(allAppsButton, -1, allAppsButton.getId(), lp, true);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // We don't want any clicks to go through to the hotseat unless the workspace is in
        // the normal state or an accessible drag is in progress.
        return !mLauncher.getWorkspace().workspaceIconsCanBeDragged() &&
                !mLauncher.getAccessibilityDelegate().isInAccessibleDrag();
    }

    @Override
    public void fillInLogContainerData(View v, ItemInfo info, Target target, Target targetParent) {
        target.gridX = info.cellX;
        target.gridY = info.cellY;
        targetParent.containerType = ContainerType.HOTSEAT;
    }

    @Override
    public void setInsets(Rect insets) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) getLayoutParams();
        DeviceProfile grid = mLauncher.getDeviceProfile();

        if (grid.isVerticalBarLayout()) {
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            if (grid.isSeascape()) {
                lp.gravity = Gravity.LEFT;
                lp.width = grid.hotseatBarSizePx + insets.left + grid.hotseatBarSidePaddingPx;
            } else {
                lp.gravity = Gravity.RIGHT;
                lp.width = grid.hotseatBarSizePx + insets.right + grid.hotseatBarSidePaddingPx;
            }
        } else {
            lp.gravity = Gravity.BOTTOM;
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = grid.hotseatBarSizePx + insets.bottom;
        }
        Rect padding = grid.getHotseatLayoutPadding();
        getLayout().setPadding(padding.left, padding.top, padding.right, padding.bottom);

        setLayoutParams(lp);
        InsettableFrameLayout.dispatchInsets(this, insets);
    }

    void updateShortcuts(ArrayList<AppInfo> infos) {
        final ViewGroup layout = getLayout().getShortcutsAndWidgets();
        InvariantDeviceProfile idp = mLauncher.getDeviceProfile().inv;


        if (categories != null) {
            for (int i = 0; i < categories.size(); i++) {
                ArrayList<String> packages = categories.get(i).packageNames;

                for (int j = 0; j < infos.size(); j++) {
                    AppInfo appInfo = infos.get(j);
                    if (isDefaultApp(appInfo)) {
                        if (packages.contains(appInfo.componentName.getPackageName())) {
                            final View view = layout.getChildAt(i);
                            if (view.getTag() instanceof FolderInfo) {
                                FolderInfo folderInfo = (FolderInfo) view.getTag();
                                folderInfo.add(new ShortcutInfo(appInfo), false);
                            }
                        }
                    } else if (isUpdateInfo(appInfo)) {
                        Log.d("HotSeat", "UpdateInfo: " + appInfo.componentName);
                    } else {
                        for (int k = 0; k < layout.getChildCount(); k++) {
                            final View view = layout.getChildAt(k);
                            if (view.getTag() instanceof FolderInfo) {
                                FolderInfo folderInfo = (FolderInfo) view.getTag();

                                if (folderInfo.screenId == idp.numHotseatIcons - 1) {
                                    for (AppInfo info : infos) {
                                        folderInfo.add(new ShortcutInfo(info), false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    boolean isDefaultApp(AppInfo info) {
        Log.d("Hotseat", "isDefaultApp: " + defaultPackages.size());
        return defaultPackages.contains(info.componentName.getPackageName());
    }

    boolean isUpdateInfo(AppInfo appInfo) {
        final ViewGroup layout = getLayout().getShortcutsAndWidgets();

        for (int i = 0; i < layout.getChildCount(); i++) {
            final View view = layout.getChildAt(i);
            if (view.getTag() instanceof FolderInfo) {
                FolderInfo folderInfo = (FolderInfo) view.getTag();

                for (int j = 0; j < folderInfo.contents.size(); j++) {
                    ShortcutInfo shortcutInfo = folderInfo.contents.get(j);
                    if (TextUtils.equals(shortcutInfo.intent.getComponent().getPackageName(), appInfo.componentName.getPackageName())) {
                        folderInfo.contents.remove(j);
                        folderInfo.contents.add(j, new ShortcutInfo(appInfo));
                        return true;
                    }
                }

                    /*for (ShortcutInfo shortcutInfo : folderInfo.contents) {
                        if (TextUtils.equals(shortcutInfo.intent.getComponent().getPackageName(), appInfo.componentName.getPackageName())) {
                            return true;
                        }
                    }*/
            }

        }

        return false;
    }

    private void parseDefaultCategories() {
        XmlResourceParser parser = mLauncher.getResources().getXml(R.xml.default_hotseat_category);
        final int depth = parser.getDepth();

        HotseatCategory hotseatCategory = null;
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
                            hotseatCategory = new HotseatCategory();
                            hotseatCategory.setCategoryName(parser.getAttributeValue(0));
                        }

                        if (parser.getName().equals("packageName")) {
                            String packageName = parser.nextText();
                            hotseatCategory.addPackage(packageName);
                            defaultPackages.add(packageName);
                        }
                        break;

                    case XmlResourceParser.END_TAG:
                        if (parser.getName().equals("category")) {
                            categories.add(hotseatCategory);
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
