package com.gdtw.imgshare.model;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class AlbumCreationRequestDTO {

    private List<MultipartFile> files;
    private int expiryDays;
    private boolean nsfw;
    private String password;
    private String clientIp;

    // Constructors
    public AlbumCreationRequestDTO() {
    }

    public AlbumCreationRequestDTO(List<MultipartFile> files, int expiryDays, boolean nsfw, String password, String clientIp) {
        this.files = files;
        this.expiryDays = expiryDays;
        this.nsfw = nsfw;
        this.password = password;
        this.clientIp = clientIp;
    }

    public List<MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }

    public int getExpiryDays() {
        return expiryDays;
    }

    public void setExpiryDays(int expiryDays) {
        this.expiryDays = expiryDays;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
