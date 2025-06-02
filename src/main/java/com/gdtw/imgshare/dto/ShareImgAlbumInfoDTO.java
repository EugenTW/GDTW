package com.gdtw.imgshare.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class ShareImgAlbumInfoDTO implements Serializable {
    private Integer siaId;
    private String siaCode;
    private String siaPassword;
    private LocalDate siaEndDate;
    private Integer siaTotalVisited;
    private Byte siaStatus;
    private Byte siaNsfw;

    public ShareImgAlbumInfoDTO() {
        // Default constructor for Jackson
    }

    public ShareImgAlbumInfoDTO(Integer siaId, String siaCode, String siaPassword, LocalDate siaEndDate, Integer siaTotalVisited, Byte siaStatus, Byte siaNsfw) {
        this.siaId = siaId;
        this.siaCode = siaCode;
        this.siaPassword = siaPassword;
        this.siaEndDate = siaEndDate;
        this.siaTotalVisited = siaTotalVisited;
        this.siaStatus = siaStatus;
        this.siaNsfw = siaNsfw;
    }

    public Integer getSiaId() {
        return siaId;
    }

    public void setSiaId(Integer siaId) {
        this.siaId = siaId;
    }

    public String getSiaCode() {
        return siaCode;
    }

    public void setSiaCode(String siaCode) {
        this.siaCode = siaCode;
    }

    public String getSiaPassword() {
        return siaPassword;
    }

    public void setSiaPassword(String siaPassword) {
        this.siaPassword = siaPassword;
    }

    public LocalDate getSiaEndDate() {
        return siaEndDate;
    }

    public void setSiaEndDate(LocalDate siaEndDate) {
        this.siaEndDate = siaEndDate;
    }

    public Integer getSiaTotalVisited() {
        return siaTotalVisited;
    }

    public void setSiaTotalVisited(Integer siaTotalVisited) {
        this.siaTotalVisited = siaTotalVisited;
    }

    public Byte getSiaStatus() {
        return siaStatus;
    }

    public void setSiaStatus(Byte siaStatus) {
        this.siaStatus = siaStatus;
    }

    public Byte getSiaNsfw() {
        return siaNsfw;
    }

    public void setSiaNsfw(Byte siaNsfw) {
        this.siaNsfw = siaNsfw;
    }

}
