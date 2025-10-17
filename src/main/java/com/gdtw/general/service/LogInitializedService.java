package com.gdtw.general.service;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LogInitializedService {

    private static final Logger http405Logger = LoggerFactory.getLogger("http405Logger");
    private static final Logger botLogger = LoggerFactory.getLogger("botLogger");

    @PostConstruct
    public void init() {
        http405Logger.info("[LogInitializedService] logger initialized.");
        botLogger.info("[LogInitializedService] logger initialized.");
    }

    @Scheduled(cron = "5 0 0 * * *")
    public void dailyLog() {
        http405Logger.info("[LogInitializedService] daily keepalive log.");
        botLogger.info("[LogInitializedService] daily keepalive log.");
    }

}
