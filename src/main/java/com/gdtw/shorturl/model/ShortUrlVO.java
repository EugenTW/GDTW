package com.gdtw.shorturl.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "short_url")
public class ShortUrlVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "su_id")
    private Integer suId;

    @Column(name = "su_original_url")
    private String suOriginalUrl;

    @Column(name = "su_shortened_url")
    private String suShortenedUrl;

    @Column(name = "su_created_date")
    private LocalDateTime suCreatedDate;

    @Column(name = "su_created_ip")
    private String suCreatedIp;

    @Column(name = "su_total_used")
    private Integer suTotalUsed;

    @Column(name = "su_status", columnDefinition = "TINYINT")
    private Byte suStatus;

    @Column(name = "su_safe")
    private String suSafe = "0";

    @Column(name = "su_reported")
    private Integer suReported = 0;

    public ShortUrlVO() {}

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

    public LocalDateTime getSuCreatedDate() {
        return suCreatedDate;
    }

    public void setSuCreatedDate(LocalDateTime suCreatedDate) {
        this.suCreatedDate = suCreatedDate;
    }

    public String getSuCreatedIp() {
        return suCreatedIp;
    }

    public void setSuCreatedIp(String suCreatedIp) {
        this.suCreatedIp = suCreatedIp;
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

    public Integer getSuReported() {
        return suReported;
    }

    public void setSuReported(Integer suReported) {
        this.suReported = suReported;
    }

}
