package com.gdtw.urlsafetycheck.service;

import com.gdtw.general.service.safebrowsing4.service.SafeBrowsingV4Service;
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
