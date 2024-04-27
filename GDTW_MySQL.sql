drop database if exists GDTW;

create database GDTW CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

use GDTW;

CREATE TABLE sort_url (
    su_id INT AUTO_INCREMENT PRIMARY KEY,
    su_original_url VARCHAR (200),    
    su_created_date DATE,
    su_created_ip VARCHAR (40),
    su_latest_used_date DATE,
    su_total_used INT DEFAULT 0,
    su_status TINYINT DEFAULT 0 CHECK (su_status IN (0, 1))
);

ALTER TABLE sort_url AUTO_INCREMENT = 10000000;

-- 應該會用Server依據資料總筆數自動刪除最舊的
-- DELIMITER //
-- CREATE TRIGGER check_pk_range
-- BEFORE INSERT ON sort_url
-- FOR EACH ROW
-- BEGIN
--     IF NEW.su_id > 21000000 THEN
--         SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: Primary key value exceeds the maximum limit!';
--     END IF;
-- END;
-- //
-- DELIMITER ;

CREATE TABLE daily_statistic (
    ds_id INT AUTO_INCREMENT PRIMARY KEY,
    ds_date DATE,
    ds_short_url_created INT,
    ds_short_url_used INT,
    ds_image_created INT, 
    ds_image_used INT,
    ds_video_created INT, 
    ds_video_used INT
);

