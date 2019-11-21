package com.android.launcher3.weather;

/**
 * 项目名   : skyLockerScreen
 * 包名     : com.sky.skylockerscreen.util
 * 文件名   : WeatherEnum
 * 创建者   :  xct
 * 创建时间 :  2019/10/9 11:26
 * 描述     :  TODO
 */
public enum WeatherEnum {
    SUNNY("晴"), // 晴

    CLOUDY("多云"), // 多云

    OVERCAST("阴"), // 阴

    FOG("雾"), // 雾


    HAZE("雾霾"),// 雾霾


    SAND_STORM("沙尘暴"), // 沙尘暴
    DUSTY("浮尘"), // 浮尘
    SAND_BLOWING("扬沙"), // 扬沙
    SAND_STORM_STRONG("强沙尘暴"), // 强沙尘暴


    RAIN_THUNDER("雷阵雨"), // 雷阵雨

    RAIN_SHOWER("阵雨"), // 阵雨
    RAIN_HAILSTONE("雷阵雨伴有冰雹"), // 雷阵雨伴有冰雹
    RAIN_ICE("冻雨"), // 冻雨
    SLEET("雨夹雪"), // 雨夹雪
    RAIN_LIGHT("小雨"), // 小雨
    RAIN_MODERATE("中雨"), // 中雨
    RAIN_HEAVY("大雨"), // 大雨
    RAIN_LIGHT_TO_MODERATE("小到中雨"), // 小到中雨
    RAIN_MODERATE_TO_HEAVY("中到大雨"), // 中到大雨
    RAIN_VERY_HEAVY("暴雨"), // 暴雨
    RAIN_DOWNPOUR("大暴雨"), // 大暴雨
    RAIN_STORM("特大暴雨"), // 特大暴雨
    RAIN_HEAVY_TO_STORM("大到暴雨"), // 大到暴雨

    SNOW_SHOWER("阵雪"), // 阵雪
    SNOW_LIGHT("小雪"), // 小雪
    SNOW_MODERATE("中雪"), // 中雪
    SNOW_HEAVY("大雪"), // 大雪
    SNOW_STORM("暴雪"), // 暴雪
    SNOW_LIGHT_TO_MODERATE("小到中雪"), // 小到中雪
    SNOW_MODERATE_TO_HEAVY("中到大雪"), // 中到大雪
    SNOW_HEAVY_TO_STORM("大到暴雪"),
    ; // 大到暴雪

    public String weather;

    WeatherEnum(String weather) {
        this.weather = weather;
    }
}