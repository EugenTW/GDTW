package com.GDTW.shorturl.model;

public class ReturnCreatedShortUrlDTO {

    private String fullShortUrl;
    private String safeUrlResult;
    private String message;

    // Constructors
    public ReturnCreatedShortUrlDTO(String fullShortUrl, String safeUrlResult, String message) {
        this.fullShortUrl = fullShortUrl;
        this.safeUrlResult = safeUrlResult;
        this.message = message;
    }

    // Getters and setters
    public String getFullShortUrl() {
        return fullShortUrl;
    }

    public void setFullShortUrl(String fullShortUrl) {
        this.fullShortUrl = fullShortUrl;
    }

    public String getSafeUrlResult() {
        return safeUrlResult;
    }

    public void setSafeUrlResult(String safeUrlResult) {
        this.safeUrlResult = safeUrlResult;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
