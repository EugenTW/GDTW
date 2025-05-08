package com.GDTW.dailystatistic.service;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_statistic")
public class DailyStatisticVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ds_id")
    private Integer dsId;

    @Column(name = "ds_date")
    private LocalDate dsDate;

    @Column(name = "ds_short_url_created")
    private Integer dsShortUrlCreated;

    @Column(name = "ds_short_url_used")
    private Integer dsShortUrlUsed;

    @Column(name = "ds_img_created")
    private Integer dsImgCreated;

    @Column(name = "ds_img_used")
    private Integer dsImgUsed;

    @Column(name = "ds_img_album_created")
    private Integer dsImgAlbumCreated;

    @Column(name = "ds_img_album_used")
    private Integer dsImgAlbumUsed;

    @Column(name = "ds_vid_created")
    private Integer dsVidCreated;

    @Column(name = "ds_vid_used")
    private Integer dsVidUsed;

    public DailyStatisticVO() {
    }

    public Integer getDsId() {
        return dsId;
    }

    public void setDsId(Integer dsId) {
        this.dsId = dsId;
    }

    public LocalDate getDsDate() {
        return dsDate;
    }

    public void setDsDate(LocalDate dsDate) {
        this.dsDate = dsDate;
    }

    public Integer getDsShortUrlCreated() {
        return dsShortUrlCreated;
    }

    public void setDsShortUrlCreated(Integer dsShortUrlCreated) {
        this.dsShortUrlCreated = dsShortUrlCreated;
    }

    public Integer getDsShortUrlUsed() {
        return dsShortUrlUsed;
    }

    public void setDsShortUrlUsed(Integer dsShortUrlUsed) {
        this.dsShortUrlUsed = dsShortUrlUsed;
    }

    public Integer getDsImgCreated() {
        return dsImgCreated;
    }

    public void setDsImgCreated(Integer dsImgCreated) {
        this.dsImgCreated = dsImgCreated;
    }

    public Integer getDsImgUsed() {
        return dsImgUsed;
    }

    public void setDsImgUsed(Integer dsImgUsed) {
        this.dsImgUsed = dsImgUsed;
    }

    public Integer getDsImgAlbumCreated() {
        return dsImgAlbumCreated;
    }

    public void setDsImgAlbumCreated(Integer dsImgAlbumCreated) {
        this.dsImgAlbumCreated = dsImgAlbumCreated;
    }

    public Integer getDsImgAlbumUsed() {
        return dsImgAlbumUsed;
    }

    public void setDsImgAlbumUsed(Integer dsImgAlbumUsed) {
        this.dsImgAlbumUsed = dsImgAlbumUsed;
    }

    public Integer getDsVidCreated() {
        return dsVidCreated;
    }

    public void setDsVidCreated(Integer dsVidCreated) {
        this.dsVidCreated = dsVidCreated;
    }

    public Integer getDsVidUsed() {
        return dsVidUsed;
    }

    public void setDsVidUsed(Integer dsVidUsed) {
        this.dsVidUsed = dsVidUsed;
    }
}
