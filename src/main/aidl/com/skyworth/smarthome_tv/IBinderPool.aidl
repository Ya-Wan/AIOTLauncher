package com.skyworth.smarthome_tv;

import android.os.IBinder;

/**
 * Describe:
 * Created by AwenZeng on 2019/2/25
 */
interface IBinderPool {
    IBinder queryBinder(String action);
}
