package com.GDTW.safebrowing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import static com.GDTW.safebrowing.service.UrlNormalizer.normalizeUrl;

@Service
public class SafeBrowsingService {

    private static final Logger logger = LoggerFactory.getLogger(SafeBrowsingService.class);

    @Value("${gcp.safeBrowsing.api}")
    private String API_KEY;

    @Value("${gcp.safeBrowsing.url}")
    private String SAFE_BROWSING_API_URL;

    private final WebClient webClient;

    public SafeBrowsingService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(SAFE_BROWSING_API_URL).build();
    }

    public String checkUrlSafety(String originalUrl) {

        // No available google api key, early return
        if (API_KEY == null || API_KEY.trim().isEmpty()) {
            return "0";
        }
        // No available google api url, early return
        if (SAFE_BROWSING_API_URL == null || SAFE_BROWSING_API_URL.trim().isEmpty()) {
            return "0";
        }

        try {
            String normalizedUrl = normalizeUrl(originalUrl);
            String requestUrl = UriComponentsBuilder
                    .fromUriString(SAFE_BROWSING_API_URL)
                    .queryParam("key", API_KEY)
                    .toUriString();

            SafeBrowsingRequest requestBody = new SafeBrowsingRequest(normalizedUrl);
            SafeBrowsingResponse response = webClient.post()
                    .uri(requestUrl)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(SafeBrowsingResponse.class)
                    .block();
            // Return 1 if the response is null or there are no matches (indicating the URL is safe)
            if (response == null || response.getMatches() == null) {
                return "1";  // Safe
            } else {
                return "2";  // Unsafe (threat detected)
            }
        } catch (WebClientResponseException ex) {
            // Handle HTTP errors, such as 403 (quota exceeded) or 503 (service unavailable)
            if (ex.getStatusCode().is4xxClientError()) {
                logger.error("Client Error: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString());
            } else if (ex.getStatusCode().is5xxServerError()) {
                logger.error("Server Error: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString());
            }
            // Return 0 in case of a client or server error (e.g., quota exceeded)
            return "0";
        } catch (WebClientRequestException ex) {
            // Handle network errors or request failures
            logger.error("Request Error: " + ex.getMessage());
            return "0";

        } catch (Exception ex) {
            // Handle any unexpected exceptions
            logger.error("Unexpected error: " + ex.getMessage());
            return "0";
        }
    }

}
