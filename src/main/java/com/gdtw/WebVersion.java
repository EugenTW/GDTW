package com.gdtw;

public class WebVersion {

    private static final String WEB_VERSION = "1.15.3";
    private static final String BUILD_DATE = "2025May13";

    private WebVersion(){}

    public static String getWebVersion() {
        return WEB_VERSION;
    }

    public static String getBuildDate() {
        return BUILD_DATE;
    }
    
}
