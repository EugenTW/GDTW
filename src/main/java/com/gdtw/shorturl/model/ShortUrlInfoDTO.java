package com.gdtw.shorturl.model;

public class ShortUrlInfoDTO {
    private Integer suId;
    private String suOriginalUrl;
    private String suShortenedUrl;
    private Integer suTotalUsed;
    private Byte suStatus;
    private String suSafe;

    public ShortUrlInfoDTO(Integer suId, String suOriginalUrl, String suShortenedUrl,
                           Integer suTotalUsed, Byte suStatus, String suSafe) {
        this.suId = suId;
        this.suOriginalUrl = suOriginalUrl;
        this.suShortenedUrl = suShortenedUrl;
        this.suTotalUsed = suTotalUsed;
        this.suStatus = suStatus;
        this.suSafe = suSafe;
    }

    public Integer getSuId() { return suId; }
    public String getSuOriginalUrl() { return suOriginalUrl; }
    public String getSuShortenedUrl() { return suShortenedUrl; }
    public Integer getSuTotalUsed() { return suTotalUsed; }
    public Byte getSuStatus() { return suStatus; }
    public String getSuSafe() { return suSafe; }
}

