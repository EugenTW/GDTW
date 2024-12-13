package com.GDTW.general.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.redfin.sitemapgenerator.WebSitemapGenerator;
import java.io.File;
import java.net.MalformedURLException;

@Service
public class SitemapService {

    @Value("${app.fullUrl}")
    private String fullUrl;

    @Value("${app.siteMapPath}")
    private String siteMapPath;

    public void generateSitemap() throws MalformedURLException {
        // Create a File object for the siteMapPath directory
        File siteMapDir = new File(siteMapPath);

        // Check if the siteMapPath directory exists; if not, create it
        if (!siteMapDir.exists()) {
            siteMapDir.mkdirs();  // Create the directory if it does not exist
        }

        // Build the sitemap generator with the specified directory as the output location
        WebSitemapGenerator sitemapGen = WebSitemapGenerator.builder(fullUrl, siteMapDir)
                .build();

        // Add URLs to the sitemap
        sitemapGen.addUrl("https://gdtw.org/");
        sitemapGen.addUrl("https://gdtw.org/index");
        sitemapGen.addUrl("https://gdtw.org/short_url");
        sitemapGen.addUrl("https://gdtw.org/short_url_redirection");
        sitemapGen.addUrl("https://gdtw.org/terms_of_service");
        sitemapGen.addUrl("https://gdtw.org/about_us");
        sitemapGen.addUrl("https://gdtw.org/contact_us");
        sitemapGen.addUrl("https://gdtw.org/error");
        sitemapGen.addUrl("https://gdtw.org/image_view");
        sitemapGen.addUrl("https://gdtw.org/image_share");
        sitemapGen.addUrl("https://gdtw.org/error_404");
        sitemapGen.addUrl("https://gdtw.org/error_410");
        sitemapGen.addUrl("https://gdtw.org/error_403_405");
        sitemapGen.addUrl("https://gdtw.org/error_generic");

        // Write the sitemap.xml file to the siteMapPath directory
        sitemapGen.write();
    }
}
