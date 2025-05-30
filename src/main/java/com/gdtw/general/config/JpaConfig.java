package com.gdtw.general.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "com.gdtw.dailystatistic.model",
        "com.gdtw.imgshare.model",
        "com.gdtw.shorturl.model",
        "com.gdtw.reportviolation.model"
})

public class JpaConfig {
}
