package com.gdtw.general.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.redfin.sitemapgenerator.WebSitemapGenerator;

import java.io.File;
import java.net.MalformedURLException;

@Service
public class SitemapService {

    private static final Logger logger = LoggerFactory.getLogger(SitemapService.class);

    @Value("${app.fullUrl}")
    private String fullUrl;

    @Value("${app.siteMapPath}")
    private String siteMapPath;

    public void generateSitemap() throws MalformedURLException {
        File siteMapDir = new File(siteMapPath);

        if (!siteMapDir.exists() && !siteMapDir.mkdirs()) {
            logger.error("Failed to create sitemap directory at path: {}.", siteMapPath);
            return;
        }

        WebSitemapGenerator sitemapGen = WebSitemapGenerator.builder(fullUrl, siteMapDir).build();

        sitemapGen.addUrl("https://gdtw.org/");
        sitemapGen.addUrl("https://gdtw.org/index");
        sitemapGen.addUrl("https://gdtw.org/short_url");
        sitemapGen.addUrl("https://gdtw.org/short_url_redirection");
        sitemapGen.addUrl("https://gdtw.org/terms_of_service");
        sitemapGen.addUrl("https://gdtw.org/about_us");
        sitemapGen.addUrl("https://gdtw.org/contact_us");
        sitemapGen.addUrl("https://gdtw.org/error");
        sitemapGen.addUrl("https://gdtw.org/img_upload");
        sitemapGen.addUrl("https://gdtw.org/img_view");
        sitemapGen.addUrl("https://gdtw.org/error_404");
        sitemapGen.addUrl("https://gdtw.org/error_410");
        sitemapGen.addUrl("https://gdtw.org/error_403_405");
        sitemapGen.addUrl("https://gdtw.org/error_generic");
        sitemapGen.addUrl("https://gdtw.org/url_safety_check");
        sitemapGen.addUrl("https://gdtw.org/cs_js_minify");

        sitemapGen.write();
    }

}
