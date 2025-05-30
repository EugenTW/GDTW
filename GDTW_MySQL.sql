DROP DATABASE IF EXISTS GDTW;

CREATE DATABASE GDTW CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE GDTW;

CREATE TABLE daily_statistic
(
    ds_id                INT AUTO_INCREMENT PRIMARY KEY,
    ds_date              DATE,
    ds_short_url_created INT,
    ds_short_url_used    INT,
    ds_img_created       INT,
    ds_img_used          INT,
    ds_img_album_created INT,
    ds_img_album_used    INT
);

CREATE TABLE short_url
(
    su_id            INT AUTO_INCREMENT PRIMARY KEY,
    su_original_url  VARCHAR(200),
    su_shortened_url VARCHAR(10),
    su_created_date  DATETIME,
    su_created_ip    VARCHAR(40),
    su_total_used    INT        DEFAULT 0,
    su_status        TINYINT    DEFAULT 0,
    su_safe          VARCHAR(1) DEFAULT 0,
    su_reported      INT        DEFAULT 0
);
ALTER TABLE short_url AUTO_INCREMENT = 11000000;

CREATE TABLE share_img_album
(
    sia_id            INT AUTO_INCREMENT PRIMARY KEY,
    sia_code          VARCHAR(100),
    sia_password      VARCHAR(10) DEFAULT NULL,
    sia_created_date  DATETIME,
    sia_created_ip    VARCHAR(40),
    sia_end_date      DATE,
    sia_total_visited INT         DEFAULT 0,
    sia_status        TINYINT     DEFAULT 0,
    sia_nsfw          TINYINT     DEFAULT 0,
    sia_reported      INT         DEFAULT 0
);
ALTER TABLE share_img_album AUTO_INCREMENT = 11000000;

CREATE TABLE share_img
(
    si_id            INT AUTO_INCREMENT PRIMARY KEY,
    si_code          VARCHAR(100),
    si_name          VARCHAR(100),
    si_password      VARCHAR(10) DEFAULT NULL,
    si_created_date  DATETIME,
    si_created_ip    VARCHAR(40),
    si_end_date      DATE,
    si_total_visited INT         DEFAULT 0,
    si_status        TINYINT     DEFAULT 0,
    si_nsfw          TINYINT     DEFAULT 0,
    si_reported      INT         DEFAULT 0,
    sia_id           INT NULL,
    FOREIGN KEY (sia_id) REFERENCES share_img_album (sia_id)
);
ALTER TABLE share_img AUTO_INCREMENT = 11000000;

CREATE TABLE violation_report (
                                  vr_id            INT AUTO_INCREMENT PRIMARY KEY,
                                  vr_ip            VARCHAR(45) NOT NULL,
                                  vr_report_type   INT         NOT NULL,
                                  vr_report_target VARCHAR(20) NOT NULL,
                                  vr_report_reason INT         NOT NULL,
                                  vr_created_time  DATETIME    DEFAULT CURRENT_TIMESTAMP,
                                  UNIQUE KEY uniq_target_ip_type (vr_report_target, vr_ip, vr_report_type)
);
ALTER TABLE violation_report AUTO_INCREMENT = 1000000;
