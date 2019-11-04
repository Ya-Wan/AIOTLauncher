package com.android.launcher3.weather;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.skyworth.framework.skysdk.ipc.SkyContext;
import com.tianci.system.api.TCSystemService;
import com.tianci.system.data.TCInfoSetData;

import static com.tianci.system.define.TCEnvKey.SKY_SYSTEM_ENV_WEATHER;

public class WeatherManager implements IWeather {
    private static final String TAG = "WeatherManager";
    private static final String WEATHER = "pref_weather";
    private Context mContext;
    private TCSystemService tcSystemService;

    private final SharedPreferences mPrefs;

    public WeatherManager(Context mContext) {
        this.mContext = mContext;
        tcSystemService = TCSystemService.getInstance(SkyContext.getListener());
        mPrefs = Utilities.getPrefs(mContext);
    }

    @Override
    public void updateWeather(ImageView weatherIv, TextView weatherCurrentTem, TextView weatherTemRange) {

        if (weatherIv == null || weatherCurrentTem == null || weatherTemRange == null) return;

        TCInfoSetData weatherData = (TCInfoSetData) tcSystemService.getSetData(SKY_SYSTEM_ENV_WEATHER);

        String weather;
        if (weatherData == null) {
            weather = mPrefs.getString(WEATHER, "");
        } else {
            weather = weatherData.getCurrent();
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putString(WEATHER, weather);
            edit.apply();
        }

        if (TextUtils.isEmpty(weather)) return;

        Log.d(TAG, "updateWeather: " + weather);

        String[] split = weather.split(",");
        String weatherIcon = split[0];
        String weatherCity = split[1];
        String weatherMinTem = split[2];
        String weatherMaxTem = split[3];
        int weatherIconRes = manageWeatherIcon(weatherIcon);
        String weatherTem = weatherMinTem + " ~ " + weatherMaxTem + " ℃";

        weatherIv.setImageResource(weatherIconRes);
        weatherCurrentTem.setText((Integer.valueOf(weatherMinTem) + Integer.valueOf(weatherMaxTem)) / 2 + " ℃");
        weatherTemRange.setText(weatherTem);
    }

    private int manageWeatherIcon(String weatherIcon) {

        int weatherIconRes = R.drawable.weather_sunny;

        switch (WeatherEnum.valueOf(weatherIcon)) {
            case CLOUDY:
                weatherIconRes = R.drawable.weather_cloudy;
                break;
            case SUNNY:
                weatherIconRes = R.drawable.weather_sunny;
                break;
            case OVERCAST:
                weatherIconRes = R.drawable.weather_overcast;
                break;
            case FOG:
                weatherIconRes = R.drawable.weather_fog;
                break;
            case HAZE:
                weatherIconRes = R.drawable.weather_haze;
                break;
            case SAND_STORM:
            case DUSTY:
            case SAND_BLOWING:
            case SAND_STORM_STRONG:
                weatherIconRes = R.drawable.weather_sand_storm;
                break;
            case RAIN_THUNDER:
                weatherIconRes = R.drawable.weather_rainthunder;
                break;

            case RAIN_SHOWER:
            case RAIN_HAILSTONE:
            case RAIN_ICE:
            case SLEET:
            case RAIN_LIGHT:
            case RAIN_MODERATE:
            case RAIN_HEAVY:
            case RAIN_LIGHT_TO_MODERATE:
            case RAIN_MODERATE_TO_HEAVY:
            case RAIN_VERY_HEAVY:
            case RAIN_DOWNPOUR:
            case RAIN_HEAVY_TO_STORM:
            case RAIN_STORM:
                weatherIconRes = R.drawable.weather_rain;
                break;

            case SNOW_HEAVY:
            case SNOW_LIGHT:
            case SNOW_STORM:
            case SNOW_SHOWER:
            case SNOW_MODERATE:
            case SNOW_HEAVY_TO_STORM:
            case SNOW_LIGHT_TO_MODERATE:
            case SNOW_MODERATE_TO_HEAVY:
                weatherIconRes = R.drawable.weather_sonw;
                break;
        }
        return weatherIconRes;

    }
}
