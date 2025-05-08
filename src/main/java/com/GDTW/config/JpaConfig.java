package com.GDTW.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "com.GDTW.dailystatistic.model",
        "com.GDTW.imgshare.model",
        "com.GDTW.shorturl.model"
})
public class JpaConfig {
}
