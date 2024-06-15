drop database if exists GDTW;

create database GDTW CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

use GDTW;

CREATE TABLE short_url (
    su_id INT AUTO_INCREMENT PRIMARY KEY,
    su_original_url VARCHAR (200),
    su_shortened_url VARCHAR (10),
    su_created_date DATE,
    su_created_ip VARCHAR (40),
    su_total_used INT DEFAULT 0,
    su_status TINYINT DEFAULT 0 CHECK (su_status IN (0, 1))
);
ALTER TABLE short_url AUTO_INCREMENT = 10000000;

CREATE TABLE share_img (
    si_id INT AUTO_INCREMENT PRIMARY KEY,
    si_address VARCHAR (200),
    si_created_date DATE,
    si_created_ip VARCHAR (40),
    si_total_visited INT DEFAULT 0,
    si_status TINYINT DEFAULT 0 CHECK (si_status IN (0, 1))
);
ALTER TABLE share_img AUTO_INCREMENT = 100000;

CREATE TABLE share_vid (
    sv_id INT AUTO_INCREMENT PRIMARY KEY,
    sv_address VARCHAR (200),
    sv_created_date DATE,
    sv_created_ip VARCHAR (40),
    sv_total_visited INT DEFAULT 0,
    sv_status TINYINT DEFAULT 0 CHECK (sv_status IN (0, 1))
);
ALTER TABLE share_vid AUTO_INCREMENT = 10000;

CREATE TABLE daily_statistic (
    ds_id INT AUTO_INCREMENT PRIMARY KEY,
    ds_date DATE,
    ds_short_url_created INT,
    ds_short_url_used INT,
    ds_img_created INT, 
    ds_img_used INT,
    ds_vid_created INT, 
    ds_vid_used INT
);

CREATE TABLE web_admin (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_name VARCHAR (30),
    admin_password VARCHAR (250),
    admin_email  VARCHAR (100),
    admin_last_logined_date DATETIME,
    admin_last_logined_ip  VARCHAR (40),
    admin_status TINYINT DEFAULT 0 CHECK (admin_status IN (0, 1)) 
);

