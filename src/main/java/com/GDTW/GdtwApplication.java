package com.GDTW;

import com.GDTW.service.SitemapService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
public class GdtwApplication {

	private static final Logger logger = LoggerFactory.getLogger(GdtwApplication.class);

	private final SitemapService sitemapService;
	public GdtwApplication(SitemapService sitemapService) {
		this.sitemapService = sitemapService;
	}

	public static void main(String[] args) {

		SpringApplication.run(GdtwApplication.class, args);
		logger.info("Web version: " + WebVersion.getWebVersion() + " and built at " + WebVersion.getBuildDate());

	}

	// Generate sitemap on application startup
	@PostConstruct
	public void generateSitemapOnStartup() {
		try {
			sitemapService.generateSitemap();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
