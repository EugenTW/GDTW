package com.GDTW.urlSafetyCheck.model;

import com.GDTW.safebrowsing4.service.SafeBrowsingV4Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UrlSafetyCheckService {
    private final SafeBrowsingV4Service safeBrowsingV4Service;

    public UrlSafetyCheckService(SafeBrowsingV4Service safeBrowsingV4Service) {
        this.safeBrowsingV4Service = safeBrowsingV4Service;
    }

    public String checkUrlSafety(String originalUrl) {
        return safeBrowsingV4Service.checkUrlSafety(originalUrl);
    }


}
