package com.gdtw.reportviolation.dto;

public class ReportRequestDTO {
    private int reportType;
    private int reportReason;
    private String targetUrl;

    public int getReportType() {
        return reportType;
    }

    public void setReportType(int reportType) {
        this.reportType = reportType;
    }

    public int getReportReason() {
        return reportReason;
    }

    public void setReportReason(int reportReason) {
        this.reportReason = reportReason;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}


