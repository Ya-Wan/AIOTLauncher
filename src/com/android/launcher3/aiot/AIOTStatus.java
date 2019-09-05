package com.android.launcher3.aiot;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.skyworth.smarthome_tv.IBinderPool;
import com.skyworth.smarthome_tv.ISmartHomeExtraInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AIOTStatus {
    public static final String TAG = "AIOTStatus";

    private Context mContext;

    private List<AodAoitEnum> firstAiotList;
    private List<AodAoitEnum> secondAiotList;
    public String air_quality;
    public String temperature;
    public String humidity;
    public String air_conditioner;
    public String air_speed;
    public List<String> notify_list;

    private ISmartHomeExtraInfo mSmartHomeExtraInfo;

    public AIOTStatus(Context context) {
        this.mContext = context;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service == null) {
                return;
            }
            IBinder iBinder = queryBinder(IBinderPool.Stub.asInterface(service),
                    "com.skyworth.aiot.ISmartHomeExtraInfo");
            mSmartHomeExtraInfo = ISmartHomeExtraInfo.Stub
                    .asInterface(iBinder);

            getSmartHomeExtraInfo();

            try {
                mSmartHomeExtraInfo.asBinder().linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public void bindAIOTService() {
        Intent intent = new Intent("com.skyworth.aiot.ConnectBinderPool");
        intent.setPackage("com.skyworth.smarthome_tv");
        mContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public void getSmartHomeExtraInfo() {
        if (mSmartHomeExtraInfo != null) {
            try {
                String temp = mSmartHomeExtraInfo.getSmartHomeExtraInfo();
                parseJsonData(temp);
            } catch (RemoteException e) {

                e.printStackTrace();
            }
        }
    }

    private void parseJsonData(String temp) {
        if (!TextUtils.isEmpty(temp)) {
            JSONObject json;
            try {
                json = new JSONObject(temp);

                String home_status = json.optString("home_status_list");
                String notify = json.optString("notify_list");
                air_quality = json.optString("air_quality");
                temperature = json.optString("temperature");
                humidity = json.optString("humidity");
                air_speed = json.optString("air_speed");
                air_conditioner = json.optString("air_conditioner");

                firstAiotList = new ArrayList<AodAoitEnum>();
                if (!TextUtils.isEmpty(temperature)) {
                    firstAiotList.add(new AodAoitEnum("temperature", temperature + "℃"));
                }
                if (!TextUtils.isEmpty(humidity)) {
                    firstAiotList.add(new AodAoitEnum("humidity", humidity));
                }
                if (!TextUtils.isEmpty(air_quality)) {
                    firstAiotList.add(new AodAoitEnum("air_quality", air_quality));
                }

                parseJsonArrayData(home_status);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public IBinder queryBinder(IBinderPool mBinderPool, String action) {
        IBinder binder = null;
        try {
            if (null != mBinderPool) {
                binder = mBinderPool.queryBinder(action);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return binder;
    }

    private void parseJsonArrayData(String home_status) {
        secondAiotList = new ArrayList<AodAoitEnum>();
        if (!TextUtils.isEmpty(home_status)) {
            JSONArray jsonList;
            try {
                jsonList = new JSONArray(home_status);
                if (jsonList.length() > 0) {
                    for (int i = 0; i < jsonList.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonList.get(i).toString());

                        String device_name = jsonObject.optString("device_name");
                        String homestatus = jsonObject.optString("homestatus");
                        if ("洗衣机".equals(device_name) || "洗碗机".equals(device_name)) {
                            if (!TextUtils.isEmpty(homestatus)) {
                                if (homestatus.length() > 2) {
                                    if (homestatus.startsWith("剩余")) {
                                        homestatus = homestatus.substring(2);
                                    } else {
                                        homestatus = "";
                                        break;
                                    }
                                }
                            }

                            secondAiotList.add(new AodAoitEnum(device_name, homestatus));
                        }
                        if ("空调".equals(device_name) || "风扇".equals(device_name)) {
                            if (!TextUtils.isEmpty(homestatus)) {
                                firstAiotList.add(new AodAoitEnum(device_name, home_status));
                            }

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binderDied");
            if (mSmartHomeExtraInfo != null) {
                mSmartHomeExtraInfo.asBinder().unlinkToDeath(this, 0);
                mSmartHomeExtraInfo = null;
            }
            bindAIOTService();
        }
    };

}
