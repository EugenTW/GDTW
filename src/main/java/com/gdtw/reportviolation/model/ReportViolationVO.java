package com.gdtw.reportviolation.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "violation_report",
        uniqueConstraints = @UniqueConstraint(name = "uniq_target_ip", columnNames = {"vr_report_target", "vr_ip"})
)
public class ReportViolationVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vr_id")
    private Integer vrId;

    @Column(name = "vr_ip", length = 45, nullable = false)
    private String vrIp;

    @Column(name = "vr_report_type", nullable = false)
    private Integer vrReportType;

    @Column(name = "vr_report_target", length = 20, nullable = false)
    private String vrReportTarget;

    @Column(name = "vr_report_reason", nullable = false)
    private Integer vrReportReason;

    @Column(name = "vr_created_time", updatable = false, insertable = false)
    private LocalDateTime vrCreatedTime;

    public ReportViolationVO() {
    }

    public Integer getVrId() {
        return vrId;
    }

    public void setVrId(Integer vrId) {
        this.vrId = vrId;
    }

    public String getVrIp() {
        return vrIp;
    }

    public void setVrIp(String vrIp) {
        this.vrIp = vrIp;
    }

    public Integer getVrReportType() {
        return vrReportType;
    }

    public void setVrReportType(Integer vrReportType) {
        this.vrReportType = vrReportType;
    }

    public String getVrReportTarget() {
        return vrReportTarget;
    }

    public void setVrReportTarget(String vrReportTarget) {
        this.vrReportTarget = vrReportTarget;
    }

    public Integer getVrReportReason() {
        return vrReportReason;
    }

    public void setVrReportReason(Integer vrReportReason) {
        this.vrReportReason = vrReportReason;
    }

    public LocalDateTime getVrCreatedTime() {
        return vrCreatedTime;
    }

    public void setVrCreatedTime(LocalDateTime vrCreatedTime) {
        this.vrCreatedTime = vrCreatedTime;
    }

}
