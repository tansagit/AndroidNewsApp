package com.app.androidnewsapp;

public class Config {

    //put your admin panel url here
    public static final String ADMIN_PANEL_URL = "http://10.0.2.2/android_news_app";

    //your api key which obtained from admin panel
    public static final String API_KEY = "cda11v2OkqSI1rhQm37PBXKnpisMtlaDzoc4w0U6uNATgZRbJG";

    //Ads Configuration
    //set true to enable or set false to disable
    public static final boolean ENABLE_ADMOB_BANNER_ADS = true;
    public static final boolean ENABLE_ADMOB_INTERSTITIAL_ADS = true;
    public static final int ADMOB_INTERSTITIAL_ADS_INTERVAL = 3;
    public static final boolean ENABLE_ADMOB_INTERSTITIAL_ADS_ON_CLICK_VIDEO = false;

    //other layout configuration
    public static final boolean ENABLE_FIXED_BOTTOM_NAVIGATION = true;
    public static final boolean ENABLE_POST_COUNT_IN_CATEGORY = false;
    public static final boolean FORCE_PLAYER_TO_LANDSCAPE = false;
    public static final boolean ENABLE_DATE_DISPLAY = true;
    public static final boolean DATE_DISPLAY_AS_TIME_AGO = false;

    //news description configuration
    public static final boolean ENABLE_TEXT_SELECTION = true;
    public static final boolean OPEN_LINK_INSIDE_APP = true;

    //if you use RTL Language e.g : Arabic Language or other, set true
    public static final boolean ENABLE_RTL_MODE = false;

    //load more for next channel list
    public static final int LOAD_MORE = 15;

    //splash screen duration in millisecond
    public static final int SPLASH_TIME = 3000;

}
