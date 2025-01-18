package com.GDTW.user.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "web_user")
public class WebUserVO{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "u_id")
    private Integer uId;

    @Column(name = "u_nickname")
    private String uNickname;

    @Column(name = "u_email")
    private String uEmail;

    @Column(name = "u_password")
    private String uPassword;

    @Column(name = "u_register_date")
    private LocalDate uRegisterDate;

    @Column(name = "u_status")
    private Byte uStatus;

    public WebUserVO() {}

    public Integer getuId() {
        return uId;
    }

    public void setuId(Integer uId) {
        this.uId = uId;
    }

    public String getuNickname() {
        return uNickname;
    }

    public void setuNickname(String uNickname) {
        this.uNickname = uNickname;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuPassword() {
        return uPassword;
    }

    public void setuPassword(String uPassword) {
        this.uPassword = uPassword;
    }

    public LocalDate getuRegisterDate() {
        return uRegisterDate;
    }

    public void setuRegisterDate(LocalDate uRegisterDate) {
        this.uRegisterDate = uRegisterDate;
    }

    public Byte getuStatus() {
        return uStatus;
    }

    public void setuStatus(Byte uStatus) {
        this.uStatus = uStatus;
    }
}
