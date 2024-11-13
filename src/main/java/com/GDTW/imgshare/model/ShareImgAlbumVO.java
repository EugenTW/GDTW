package com.GDTW.imgshare.model;

import com.GDTW.user.model.WebUserVO;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "share_img_album")
public class ShareImgAlbumVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sia_id")
    private Integer siaId;

    @Column(name = "sia_code", length = 100)
    private String siaCode;

    @Column(name = "sia_password", length = 10)
    private String siaPassword;

    @Column(name = "sia_created_date")
    private LocalDate siaCreatedDate;

    @Column(name = "sia_created_ip", length = 40)
    private String siaCreatedIp;

    @Column(name = "sia_end_date")
    private LocalDate siaEndDate;

    @Column(name = "sia_total_visited")
    private Integer siaTotalVisited = 0;

    @Column(name = "sia_status")
    private Byte siaStatus = 0;

    @Column(name = "sia_nsfw")
    private Byte siaNsfw = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "u_id", referencedColumnName = "u_id")
    private WebUserVO user;

    public ShareImgAlbumVO() {
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

    public LocalDate getSiaCreatedDate() {
        return siaCreatedDate;
    }

    public void setSiaCreatedDate(LocalDate siaCreatedDate) {
        this.siaCreatedDate = siaCreatedDate;
    }

    public String getSiaCreatedIp() {
        return siaCreatedIp;
    }

    public void setSiaCreatedIp(String siaCreatedIp) {
        this.siaCreatedIp = siaCreatedIp;
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

    public WebUserVO getUser() {
        return user;
    }

    public void setUser(WebUserVO user) {
        this.user = user;
    }
}
