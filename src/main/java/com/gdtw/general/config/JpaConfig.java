package com.gdtw.general.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "com.gdtw.dailystatistic.repository",
        "com.gdtw.imgshare.model",
        "com.gdtw.shorturl.repository",
        "com.gdtw.reportviolation.repository"
})

public class JpaConfig {
}
