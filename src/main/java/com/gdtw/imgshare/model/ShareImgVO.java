package com.gdtw.imgshare.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "share_img")
public class ShareImgVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "si_id")
    private Integer siId;

    @Column(name = "si_code", length = 100)
    private String siCode;

    @Column(name = "si_name", length = 100)
    private String siName;

    @Column(name = "si_password", length = 10)
    private String siPassword;

    @Column(name = "si_created_date")
    private LocalDateTime siCreatedDate;

    @Column(name = "si_created_ip", length = 40)
    private String siCreatedIp;

    @Column(name = "si_end_date")
    private LocalDate siEndDate;

    @Column(name = "si_total_visited", nullable = false)
    private Integer siTotalVisited = 0;

    @Column(name = "si_status", nullable = false)
    private Byte siStatus = 0;

    @Column(name = "si_nsfw", nullable = false)
    private Byte siNsfw = 0;

    @Column(name = "si_reported")
    private Integer siReported = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sia_id", referencedColumnName = "sia_id")
    private ShareImgAlbumVO album;

    public ShareImgVO() {
    }

    public Integer getSiId() {
        return siId;
    }

    public void setSiId(Integer siId) {
        this.siId = siId;
    }

    public String getSiCode() {
        return siCode;
    }

    public void setSiCode(String siCode) {
        this.siCode = siCode;
    }

    public String getSiName() {
        return siName;
    }

    public void setSiName(String siName) {
        this.siName = siName;
    }

    public String getSiPassword() {
        return siPassword;
    }

    public void setSiPassword(String siPassword) {
        this.siPassword = siPassword;
    }

    public LocalDateTime getSiCreatedDate() {
        return siCreatedDate;
    }

    public void setSiCreatedDate(LocalDateTime siCreatedDate) {
        this.siCreatedDate = siCreatedDate;
    }

    public String getSiCreatedIp() {
        return siCreatedIp;
    }

    public void setSiCreatedIp(String siCreatedIp) {
        this.siCreatedIp = siCreatedIp;
    }

    public LocalDate getSiEndDate() {
        return siEndDate;
    }

    public void setSiEndDate(LocalDate siEndDate) {
        this.siEndDate = siEndDate;
    }

    public Integer getSiTotalVisited() {
        return siTotalVisited;
    }

    public void setSiTotalVisited(Integer siTotalVisited) {
        this.siTotalVisited = siTotalVisited;
    }

    public Byte getSiStatus() {
        return siStatus;
    }

    public void setSiStatus(Byte siStatus) {
        this.siStatus = siStatus;
    }

    public Byte getSiNsfw() {
        return siNsfw;
    }

    public void setSiNsfw(Byte siNsfw) {
        this.siNsfw = siNsfw;
    }

    public ShareImgAlbumVO getAlbum() {
        return album;
    }

    public void setAlbum(ShareImgAlbumVO album) {
        this.album = album;
    }

    public Integer getSiReported() {
        return siReported;
    }

    public void setSiReported(Integer siReported) {
        this.siReported = siReported;
    }
}
