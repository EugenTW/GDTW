package com.gdtw.shorturl.dto;

import java.io.Serializable;

public class ShortUrlInfoDTO implements Serializable {
    private Integer suId;
    private String suOriginalUrl;
    private String suShortenedUrl;
    private Integer suTotalUsed;
    private Byte suStatus;
    private String suSafe;

    public ShortUrlInfoDTO() {
        // Default constructor for Jackson
    }

    public ShortUrlInfoDTO(Integer suId, String suOriginalUrl, String suShortenedUrl,
                           Integer suTotalUsed, Byte suStatus, String suSafe) {
        this.suId = suId;
        this.suOriginalUrl = suOriginalUrl;
        this.suShortenedUrl = suShortenedUrl;
        this.suTotalUsed = suTotalUsed;
        this.suStatus = suStatus;
        this.suSafe = suSafe;
    }

    public Integer getSuId() {
        return suId;
    }

    public void setSuId(Integer suId) {
        this.suId = suId;
    }

    public String getSuOriginalUrl() {
        return suOriginalUrl;
    }

    public void setSuOriginalUrl(String suOriginalUrl) {
        this.suOriginalUrl = suOriginalUrl;
    }

    public String getSuShortenedUrl() {
        return suShortenedUrl;
    }

    public void setSuShortenedUrl(String suShortenedUrl) {
        this.suShortenedUrl = suShortenedUrl;
    }

    public Integer getSuTotalUsed() {
        return suTotalUsed;
    }

    public void setSuTotalUsed(Integer suTotalUsed) {
        this.suTotalUsed = suTotalUsed;
    }

    public Byte getSuStatus() {
        return suStatus;
    }

    public void setSuStatus(Byte suStatus) {
        this.suStatus = suStatus;
    }

    public String getSuSafe() {
        return suSafe;
    }

    public void setSuSafe(String suSafe) {
        this.suSafe = suSafe;
    }

}

