package com.gdtw.general.service.safebrowsing4;

import com.gdtw.general.util.UrlNormalizerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SafeBrowsingV4Service {

    private static final Logger logger = LoggerFactory.getLogger(SafeBrowsingV4Service.class);

    @Value("${gcp.safeBrowsing.api}")
    private String apiKey;

    @Value("${gcp.safeBrowsingV4.url}")
    private String safeBrowsingV4ApiUrl;

    private final WebClient webClient;

    public SafeBrowsingV4Service(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(safeBrowsingV4ApiUrl).build();
    }

    public String checkUrlSafety(String originalUrl) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "0";
        }

        if (safeBrowsingV4ApiUrl == null || safeBrowsingV4ApiUrl.trim().isEmpty()) {
            return "0";
        }

        try {
            String normalizedUrl = UrlNormalizerUtil.normalizeUrl(originalUrl);
            String requestUrl = UriComponentsBuilder
                    .fromUriString(safeBrowsingV4ApiUrl)
                    .queryParam("key", apiKey)
                    .toUriString();

            SafeBrowsingV4RequestDTO requestBody = new SafeBrowsingV4RequestDTO(normalizedUrl);
            SafeBrowsingV4ResponseDTO response = webClient.post()
                    .uri(requestUrl)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(SafeBrowsingV4ResponseDTO.class)
                    .block();

            if (response == null || response.getMatches() == null) {
                return "1";  // Safe
            } else {
                return "2";  // Unsafe
            }

        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                logger.error("Client Error: {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            } else if (ex.getStatusCode().is5xxServerError()) {
                logger.error("Server Error: {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            }
            return "0";
        } catch (WebClientRequestException ex) {
            logger.error("Request Error: {}", ex.getMessage());
            return "0";
        } catch (Exception ex) {
            logger.error("Unexpected error: {}", ex.getMessage());
            return "0";
        }
    }

}
