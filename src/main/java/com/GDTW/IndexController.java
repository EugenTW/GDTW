package com.GDTW;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


@Controller
public class IndexController {

    @GetMapping("/")
    public String index1() {
        return "forward:/index.html";
    }

    @GetMapping("/index")
    public String index2() {
        return "forward:/index.html";
    }

    @GetMapping("/about_us")
    public String aboutUs() {
        return "forward:/about_us.html";
    }

    @GetMapping("/terms_of_service")
    public String turnOfService() {
        return "forward:/terms_of_service.html";
    }

    @GetMapping("/contact_us")
    public String contactUs() {
        return "forward:/contact_us.html";
    }

    @GetMapping("/show_statistics")
    public String showStatistics() {
        return "forward:/show_statistics.html";
    }

    @GetMapping("/sitemap.xml")
    public ResponseEntity<Resource> getSitemap() {
        // Determine the base directory (current working directory)
        String baseDir = System.getProperty("user.dir");

        // Specify the path to the sitemap.xml file
        Path sitemapPath = Paths.get(baseDir, "logs", "sitemap.xml");

        // Create a File object from the path
        File sitemapFile = sitemapPath.toFile();

        if (sitemapFile.exists()) {
            // If the file exists, create a resource to serve the file
            Resource resource = new FileSystemResource(sitemapFile);

            // Set the content type to XML and return the file as a response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } else {
            // If the file does not exist, return a 404 Not Found response
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
