package com.GDTW.service;

import org.springframework.stereotype.Service;

import com.redfin.sitemapgenerator.WebSitemapGenerator;

import java.io.File;
import java.net.MalformedURLException;

@Service
public class SitemapService {

    public void generateSitemap() throws MalformedURLException {
        WebSitemapGenerator sitemapGen = WebSitemapGenerator.builder("https://gdtw.org/", new File("target/"))
                .build();
        sitemapGen.addUrl("https://gdtw.org/short_url");
        sitemapGen.addUrl("https://gdtw.org/short_url_redirection");
        sitemapGen.addUrl("https://gdtw.org/terms_of_use");
        sitemapGen.addUrl("https://gdtw.org/about_us");

        sitemapGen.write();
    }
}
