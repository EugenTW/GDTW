package com.GDTW.shorturl.model;

import com.GDTW.user.model.WebUserVO;
import jakarta.persistence.*;

import java.util.Date;

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
    private Date suCreatedDate;

    @Column(name = "su_created_ip")
    private String suCreatedIp;

    @Column(name = "su_total_used")
    private Integer suTotalUsed;

    @Column(name = "su_status")
    private Integer suStatus = 0;

    @Column(name = "su_safe")
    private String suSafe = "0";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_Id", referencedColumnName = "u_id")
    private WebUserVO user;

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

    public Date getSuCreatedDate() {
        return suCreatedDate;
    }

    public void setSuCreatedDate(Date suCreatedDate) {
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

    public Integer getSuStatus() {
        return suStatus;
    }

    public void setSuStatus(Integer suStatus) {
        this.suStatus = suStatus;
    }

    public String getSuSafe() {
        return suSafe;
    }

    public void setSuSafe(String suSafe) {
        this.suSafe = suSafe;
    }

    public WebUserVO getUser() {
        return user;
    }

    public void setUser(WebUserVO user) {
        this.user = user;
    }

}
