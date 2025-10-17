package com.gdtw.general.config;

import com.gdtw.general.filter.bot.BotDetectionFilter;
import com.gdtw.general.filter.bot.BotFilterProperties;
import com.gdtw.general.filter.http405.MethodNotAllowedLoggingFilter;
import com.gdtw.general.filter.HostValidationFilter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public HostValidationFilter hostValidationFilter() {
        return new HostValidationFilter();
    }

    @Bean
    public MethodNotAllowedLoggingFilter methodNotAllowedLoggingFilter() {
        return new MethodNotAllowedLoggingFilter();
    }

    @Bean
    public BotDetectionFilter botDetectionFilter(BotFilterProperties config) {
        return new BotDetectionFilter(config);
    }

    @Bean
    public FilterRegistrationBean<HostValidationFilter> hostValidationFilterRegistration(HostValidationFilter filter) {
        FilterRegistrationBean<HostValidationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<MethodNotAllowedLoggingFilter> methodNotAllowedFilterRegistration(MethodNotAllowedLoggingFilter filter) {
        FilterRegistrationBean<MethodNotAllowedLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<BotDetectionFilter> botDetectionFilterRegistration(BotDetectionFilter filter) {
        FilterRegistrationBean<BotDetectionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(3);
        return registrationBean;
    }

}