package com.gdtw.shorturl.model;

public class ReturnOriginalUrlDTO {

    private String originalUrl;
    private String originalUrlSafe;
    private String errorMessage;

    public ReturnOriginalUrlDTO(String originalUrl, String originalUrlSafe, String errorMessage) {
        this.originalUrl = originalUrl;
        this.originalUrlSafe = originalUrlSafe;
        this.errorMessage = errorMessage;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getOriginalUrlSafe() {
        return originalUrlSafe;
    }

    public void setOriginalUrlSafe(String originalUrlSafe) {
        this.originalUrlSafe = originalUrlSafe;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
