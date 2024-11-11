drop
database if exists GDTW;

create
database GDTW CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

use
GDTW;

CREATE TABLE web_user
(
    u_id            INT AUTO_INCREMENT PRIMARY KEY,
    u_nickname      VARCHAR(25),
    u_email         VARCHAR(100),
    u_password      VARCHAR(100),
    u_register_date DATE,
    u_status        TINYINT DEFAULT 0 CHECK (u_status IN (0, 2))
);
ALTER TABLE web_user AUTO_INCREMENT = 1000000;

CREATE TABLE short_url
(
    su_id            INT AUTO_INCREMENT PRIMARY KEY,
    su_original_url  VARCHAR(200),
    su_shortened_url VARCHAR(10),
    su_created_date  DATE,
    su_created_ip    VARCHAR(40),
    su_total_used    INT        DEFAULT 0,
    su_status        TINYINT    DEFAULT 0,
    su_safe          VARCHAR(1) DEFAULT 0,
    u_id             INT NULL,
    FOREIGN KEY (u_id) REFERENCES web_user (u_id)
);
ALTER TABLE short_url AUTO_INCREMENT = 10000000;

CREATE TABLE img_album
(
    ab_id            INT AUTO_INCREMENT PRIMARY KEY,
    ab_code          VARCHAR(100),
    ab_password      VARCHAR(10) DEFAULT NULL,
    ab_created_date  DATE,
    ab_created_ip    VARCHAR(40),
    ab_end_date      DATE,
    ab_total_visited INT         DEFAULT 0,
    ab_status        TINYINT     DEFAULT 0,
    u_id             INT NULL,
    FOREIGN KEY (u_id) REFERENCES web_user (u_id)
);
ALTER TABLE img_album AUTO_INCREMENT = 10000000;

CREATE TABLE share_img
(
    si_id            INT AUTO_INCREMENT PRIMARY KEY,
    si_code          VARCHAR(100),
    si_password      VARCHAR(10) DEFAULT NULL,
    si_created_date  DATE,
    si_created_ip    VARCHAR(40),
    si_end_date      DATE,
    si_total_visited INT         DEFAULT 0,
    si_status        TINYINT     DEFAULT 0,
    u_id             INT NULL,
    ab_id            INT NULL,
    FOREIGN KEY (u_id) REFERENCES web_user (u_id),
    FOREIGN KEY (ab_id) REFERENCES img_album (ab_id)
);
ALTER TABLE share_img AUTO_INCREMENT = 10000000;

CREATE TABLE share_vid
(
    sv_id            INT AUTO_INCREMENT PRIMARY KEY,
    sv_code          VARCHAR(100),
    sv_password      VARCHAR(10),
    sv_created_date  DATE,
    sv_created_ip    VARCHAR(40),
    sv_end_date      DATE,
    sv_total_visited INT     DEFAULT 0,
    sv_status        TINYINT DEFAULT 0,
    u_id             INT NULL,
    FOREIGN KEY (u_id) REFERENCES web_user (u_id)
);
ALTER TABLE share_vid AUTO_INCREMENT = 10000000;

CREATE TABLE daily_statistic
(
    ds_id                 INT AUTO_INCREMENT PRIMARY KEY,
    ds_date               DATE,
    ds_short_url_created  INT,
    ds_short_url_used     INT,
    ds_img_created        INT,
    ds_img_used           INT,
    ds_img_album_created INT,
    ds_img_album_used    INT,
    ds_vid_created        INT,
    ds_vid_used           INT
);

CREATE TABLE web_admin
(
    am_id                INT AUTO_INCREMENT PRIMARY KEY,
    am_nickname          VARCHAR(30),
    am_password          VARCHAR(100),
    am_email             VARCHAR(100),
    am_last_logined_date DATETIME,
    am_last_logined_ip   VARCHAR(40),
    am_status            TINYINT DEFAULT 0
);
ALTER TABLE web_admin AUTO_INCREMENT = 100;
