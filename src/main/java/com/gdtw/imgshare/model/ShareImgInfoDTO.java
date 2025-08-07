package com.gdtw.imgshare.model;

import java.io.Serializable;
import java.time.LocalDate;

public class ShareImgInfoDTO implements Serializable {
    private Integer siId;
    private String siCode;
    private String siName;
    private String siPassword;
    private LocalDate siEndDate;
    private Integer siTotalVisited;
    private Byte siStatus;
    private Byte siNsfw;
    private Integer albumId;

    public ShareImgInfoDTO() {
        // Default constructor for Jackson
    }

    public ShareImgInfoDTO(Integer siId, String siCode, String siName, String siPassword, LocalDate siEndDate, Integer siTotalVisited, Byte siStatus, Byte siNsfw, Integer albumId) {
        this.siId = siId;
        this.siCode = siCode;
        this.siName = siName;
        this.siPassword = siPassword;
        this.siEndDate = siEndDate;
        this.siTotalVisited = siTotalVisited;
        this.siStatus = siStatus;
        this.siNsfw = siNsfw;
        this.albumId = albumId;
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

    public Integer getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Integer albumId) {
        this.albumId = albumId;
    }

}
