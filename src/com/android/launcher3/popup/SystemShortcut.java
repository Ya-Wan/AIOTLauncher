package com.android.launcher3.popup;

import static com.android.launcher3.userevent.nano.LauncherLogProto.Action;
import static com.android.launcher3.userevent.nano.LauncherLogProto.ControlType;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.model.WidgetItem;
import com.android.launcher3.util.InstantAppResolver;
import com.android.launcher3.util.PackageManagerHelper;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.widget.WidgetsBottomSheet;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Represents a system shortcut for a given app. The shortcut should have a static label and
 * icon, and an onClickListener that depends on the item that the shortcut services.
 *
 * Example system shortcuts, defined as inner classes, include Widgets and AppInfo.
 */
public abstract class SystemShortcut<T extends BaseDraggingActivity> extends ItemInfo {
    public final int iconResId;
    public final int labelResId;

    public SystemShortcut(int iconResId, int labelResId) {
        this.iconResId = iconResId;
        this.labelResId = labelResId;
    }

    public abstract View.OnClickListener getOnClickListener(T activity, ItemInfo itemInfo);

    public static class Widgets extends SystemShortcut<Launcher> {

        public Widgets() {
            super(R.drawable.ic_widget, R.string.widget_button_text);
        }

        @Override
        public View.OnClickListener getOnClickListener(final Launcher launcher,
                final ItemInfo itemInfo) {
            final List<WidgetItem> widgets =
                    launcher.getPopupDataProvider().getWidgetsForPackageUser(new PackageUserKey(
                            itemInfo.getTargetComponent().getPackageName(), itemInfo.user));
            if (widgets == null) {
                return null;
            }
            return (view) -> {
                AbstractFloatingView.closeAllOpenViews(launcher);
                WidgetsBottomSheet widgetsBottomSheet =
                        (WidgetsBottomSheet) launcher.getLayoutInflater().inflate(
                                R.layout.widgets_bottom_sheet, launcher.getDragLayer(), false);
                widgetsBottomSheet.populateAndShow(itemInfo);
                launcher.getUserEventDispatcher().logActionOnControl(Action.Touch.TAP,
                        ControlType.WIDGETS_BUTTON, view);
            };
        }
    }

    public static class AppInfo extends SystemShortcut {
        public AppInfo() {
            super(R.drawable.ic_info_no_shadow, R.string.app_info_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(
                BaseDraggingActivity activity, ItemInfo itemInfo) {
            return (view) -> {
                dismissTaskMenuView(activity);
                Rect sourceBounds = activity.getViewBounds(view);
                Bundle opts = activity.getActivityLaunchOptionsAsBundle(view);
                new PackageManagerHelper(activity).startDetailsActivityForInfo(
                        itemInfo, sourceBounds, opts);
                activity.getUserEventDispatcher().logActionOnControl(Action.Touch.TAP,
                        ControlType.APPINFO_TARGET, view);
            };
        }
    }

    public static class Install extends SystemShortcut {
        public Install() {
            super(R.drawable.ic_install_no_shadow, R.string.install_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(
                BaseDraggingActivity activity, ItemInfo itemInfo) {
            boolean supportsWebUI = (itemInfo instanceof ShortcutInfo) &&
                    ((ShortcutInfo) itemInfo).hasStatusFlag(ShortcutInfo.FLAG_SUPPORTS_WEB_UI);
            boolean isInstantApp = false;
            if (itemInfo instanceof com.android.launcher3.AppInfo) {
                com.android.launcher3.AppInfo appInfo = (com.android.launcher3.AppInfo) itemInfo;
                isInstantApp = InstantAppResolver.newInstance(activity).isInstantApp(appInfo);
            }
            boolean enabled = supportsWebUI || isInstantApp;
            if (!enabled) {
                return null;
            }
            return createOnClickListener(activity, itemInfo);
        }

        public View.OnClickListener createOnClickListener(
                BaseDraggingActivity activity, ItemInfo itemInfo) {
            return view -> {
                Intent intent = new PackageManagerHelper(view.getContext()).getMarketIntent(
                        itemInfo.getTargetComponent().getPackageName());
                activity.startActivitySafely(view, intent, itemInfo);
                AbstractFloatingView.closeAllOpenViews(activity);
            };
        }
    }

    protected static void dismissTaskMenuView(BaseDraggingActivity activity) {
        AbstractFloatingView.closeOpenViews(activity, true,
            AbstractFloatingView.TYPE_ALL & ~AbstractFloatingView.TYPE_REBIND_SAFE);
    }

public static class UnInstall extends SystemShortcut {
        public UnInstall() {
            super(R.drawable.ic_uninstall_no_shadow, R.string.uninstall_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(
                BaseDraggingActivity activity, ItemInfo itemInfo) {
            ComponentName cn = getUninstallTarget(itemInfo, activity);
            if (cn == null) {
                return null;
            }

            return view -> {
                AbstractFloatingView.closeAllOpenViews(activity);
                try {
                    Intent i = Intent.parseUri(activity.getString(R.string.delete_package_intent), 0)
                            .setData(Uri.fromParts("package", cn.getPackageName(), cn.getClassName()))
                            .putExtra(Intent.EXTRA_USER, itemInfo.user);
                    activity.startActivity(i);
                } catch (URISyntaxException e) {
                    Log.e("UnInstall", "Failed to parse intent to start uninstall activity for item=" + itemInfo);
                }
            };
        }

        private ComponentName getUninstallTarget(ItemInfo item, Context context) {
            Intent intent = null;
            UserHandle user = null;
            if (item != null &&
                    item.itemType == LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION) {
                intent = item.getIntent();
                user = item.user;
            }
            if (intent != null) {
                LauncherActivityInfo info = LauncherAppsCompat.getInstance(context)
                        .resolveActivity(intent, user);
                if (info != null
                        && (info.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    return info.getComponentName();
                }
            }
            return null;
        }
    }

    public static class MoveToEdu extends SystemShortcut {
        public MoveToEdu() {
            super(R.drawable.ic_uninstall_no_shadow, R.string.move_edu_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(
                BaseDraggingActivity activity, ItemInfo itemInfo) {

            return view -> {
                dismissTaskMenuView(activity);
                ((Launcher)activity).notifyUpdate((com.android.launcher3.ShortcutInfo) itemInfo, 0);
            };
        }

    }

    public static class MoveToWork extends SystemShortcut {
        public MoveToWork() {
            super(R.drawable.ic_uninstall_no_shadow, R.string.move_work_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(
                BaseDraggingActivity activity, ItemInfo itemInfo) {

            return view -> {
                dismissTaskMenuView(activity);
                ((Launcher)activity).notifyUpdate((com.android.launcher3.ShortcutInfo) itemInfo,  1);
            };
        }

    }

    public static class MoveToEntertainment extends SystemShortcut {
        public MoveToEntertainment() {
            super(R.drawable.ic_uninstall_no_shadow, R.string.move_entertainment_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(
                BaseDraggingActivity activity, ItemInfo itemInfo) {

            return view -> {
                dismissTaskMenuView(activity);
                ((Launcher)activity).notifyUpdate((com.android.launcher3.ShortcutInfo) itemInfo,  2);
            };
        }

    }

    public static class MoveToLife extends SystemShortcut {
        public MoveToLife() {
            super(R.drawable.ic_uninstall_no_shadow, R.string.move_life_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(
                BaseDraggingActivity activity, ItemInfo itemInfo) {

            return view -> {
                dismissTaskMenuView(activity);
                ((Launcher)activity).notifyUpdate((com.android.launcher3.ShortcutInfo) itemInfo, 3);
            };
        }

    }

    public static class MoveToMore extends SystemShortcut {
        public MoveToMore() {
            super(R.drawable.ic_uninstall_no_shadow, R.string.move_more_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(
                BaseDraggingActivity activity, ItemInfo itemInfo) {

            return view -> {
                dismissTaskMenuView(activity);
                ((Launcher)activity).notifyUpdate((com.android.launcher3.ShortcutInfo) itemInfo, 4);
            };
        }

    }
}
