package com.gdtw.general.helper.ratelimiter;

public enum RateLimitRule {
    SHORTURL_CREATE("shorturl:create", 30, 60),
    SHORTURL_GET("shorturl:get", 300, 60),
    SHAREIMAGE_CREATE("shareimage:create", 15, 60),
    SHAREIMAGE_GET("shareimage:get", 150, 60),
    STATISTIC_GET("statistic:get", 150, 60),
    URLSAFE_GET("urlsafe:get", 10, 60),
    CSS_JS_MINIFY_CREATE("cssjs:create", 30, 60),
    REPORT_VIOLATION_CREATE("reportviolation:create", 10, 60);

    public final String actionKey;
    public final int limit;
    public final int durationSeconds;

    RateLimitRule(String actionKey, int limit, int durationSeconds) {
        this.actionKey = actionKey;
        this.limit = limit;
        this.durationSeconds = durationSeconds;
    }

}
