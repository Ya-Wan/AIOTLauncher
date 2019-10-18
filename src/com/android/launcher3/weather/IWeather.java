package com.android.launcher3.weather;

import android.widget.ImageView;
import android.widget.TextView;

public interface IWeather {

    void updateWeather(ImageView weatherIv, TextView weatherCurrentTem, TextView weatherTemRange);
}
