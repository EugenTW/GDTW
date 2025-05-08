package com.GDTW.general.service.ratelimiter;

public enum RateLimitRule {
    SHORTURL_CREATE("shorturl:create", 30, 60),
    SHORTURL_GET("shorturl:get", 400, 60),
    SHAREIMAGE_CREATE("shareimage:create", 15, 60),
    SHAREIMAGE_GET("shareimage:get", 250, 60),
    STATISTIC_GET("statistic:get", 250, 60),
    URLSAFE_GET("urlsafe:get", 30, 60);

    public final String actionKey;
    public final int limit;
    public final int durationSeconds;

    RateLimitRule(String actionKey, int limit, int durationSeconds) {
        this.actionKey = actionKey;
        this.limit = limit;
        this.durationSeconds = durationSeconds;
    }

}
