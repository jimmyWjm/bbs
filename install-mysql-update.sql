-- ----------------------------
-- 老版本2.0 升级用
-- ----------------------------
ALTER TABLE `bbs`.`bbs_post` 
ADD COLUMN `pros` INT(11) NULL DEFAULT 0 COMMENT '' AFTER `update_time`,
ADD COLUMN `cons` INT(11) NULL DEFAULT 0 COMMENT '' AFTER `pros`,
ADD COLUMN `is_accept` INT(11) NULL DEFAULT 0 COMMENT '' AFTER `cons`;



-- ----------------------------
-- 某些模板只读，20190428
-- ----------------------------
ALTER TABLE `bbs`.`bbs_module`
ADD COLUMN `readonly` INT NULL DEFAULT 0 COMMENT '' AFTER `turn`,
ADD COLUMN `admin_list` VARCHAR(120) NULL COMMENT '' AFTER `readonly`;