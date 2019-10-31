package com.android.launcher3.allapps.classes;

import android.content.Context;
import android.os.Process;
import android.os.UserHandle;

import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.util.LabelComparator;

import java.util.Comparator;

public class ShortcutInfoComparator implements Comparator<ShortcutInfo> {
    private final UserManagerCompat mUserManager;
    private final UserHandle mMyUser;
    private final LabelComparator mLabelComparator;

    public ShortcutInfoComparator(Context context) {
        mUserManager = UserManagerCompat.getInstance(context);
        mMyUser = Process.myUserHandle();
        mLabelComparator = new LabelComparator();
    }

    @Override
    public int compare(ShortcutInfo a, ShortcutInfo b) {
        // Order by the title in the current locale
        int result = mLabelComparator.compare(a.title.toString(), b.title.toString());
        if (result != 0) {
            return result;
        }

        // If labels are same, compare component names
        result = a.getTargetComponent().compareTo(b.getTargetComponent());
        if (result != 0) {
            return result;
        }

        if (mMyUser.equals(a.user)) {
            return -1;
        } else {
            Long aUserSerial = mUserManager.getSerialNumberForUser(a.user);
            Long bUserSerial = mUserManager.getSerialNumberForUser(b.user);
            return aUserSerial.compareTo(bUserSerial);
        }
    }
}
