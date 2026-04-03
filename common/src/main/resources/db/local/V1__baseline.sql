-- Timestamp columns to BIGINT UNSIGNED
ALTER TABLE ${playerBans} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `updated` `updated` BIGINT UNSIGNED, CHANGE `expires` `expires` BIGINT UNSIGNED;
ALTER TABLE ${playerBanRecords} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `pastCreated` `pastCreated` BIGINT UNSIGNED, CHANGE `expired` `expired` BIGINT UNSIGNED;
ALTER TABLE ${playerMutes} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `updated` `updated` BIGINT UNSIGNED, CHANGE `expires` `expires` BIGINT UNSIGNED;
ALTER TABLE ${playerMuteRecords} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `pastCreated` `pastCreated` BIGINT UNSIGNED, CHANGE `expired` `expired` BIGINT UNSIGNED;
ALTER TABLE ${playerWarnings} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `expires` `expires` BIGINT UNSIGNED;
ALTER TABLE ${playerReports} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `updated` `updated` BIGINT UNSIGNED;
ALTER TABLE ${playerReportComments} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `updated` `updated` BIGINT UNSIGNED;
ALTER TABLE ${playerReportCommands} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `updated` `updated` BIGINT UNSIGNED;
ALTER TABLE ${playerKicks} CHANGE `created` `created` BIGINT UNSIGNED;
ALTER TABLE ${playerNotes} CHANGE `created` `created` BIGINT UNSIGNED;
ALTER TABLE ${players} CHANGE `lastSeen` `lastSeen` BIGINT UNSIGNED;
ALTER TABLE ${playerHistory} CHANGE `join` `join` BIGINT UNSIGNED, CHANGE `leave` `leave` BIGINT UNSIGNED;
ALTER TABLE ${ipBans} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `updated` `updated` BIGINT UNSIGNED, CHANGE `expires` `expires` BIGINT UNSIGNED;
ALTER TABLE ${ipBanRecords} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `pastCreated` `pastCreated` BIGINT UNSIGNED, CHANGE `expired` `expired` BIGINT UNSIGNED;
ALTER TABLE ${ipMutes} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `updated` `updated` BIGINT UNSIGNED, CHANGE `expires` `expires` BIGINT UNSIGNED;
ALTER TABLE ${ipMuteRecords} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `pastCreated` `pastCreated` BIGINT UNSIGNED, CHANGE `expired` `expired` BIGINT UNSIGNED;
ALTER TABLE ${ipRangeBans} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `updated` `updated` BIGINT UNSIGNED, CHANGE `expires` `expires` BIGINT UNSIGNED;
ALTER TABLE ${ipRangeBanRecords} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `pastCreated` `pastCreated` BIGINT UNSIGNED, CHANGE `expired` `expired` BIGINT UNSIGNED;
ALTER TABLE ${nameBans} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `updated` `updated` BIGINT UNSIGNED, CHANGE `expires` `expires` BIGINT UNSIGNED;
ALTER TABLE ${nameBanRecords} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `pastCreated` `pastCreated` BIGINT UNSIGNED, CHANGE `expired` `expired` BIGINT UNSIGNED;
ALTER TABLE ${rollbacks} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `expires` `expires` BIGINT UNSIGNED;

-- Silent column
ALTER TABLE ${playerBans} ADD COLUMN `silent` TINYINT(1);
ALTER TABLE ${playerBanRecords} ADD COLUMN `silent` TINYINT(1);
ALTER TABLE ${playerMutes} ADD COLUMN `silent` TINYINT(1);
ALTER TABLE ${playerMuteRecords} ADD COLUMN `silent` TINYINT(1);
ALTER TABLE ${ipBans} ADD COLUMN `silent` TINYINT(1);
ALTER TABLE ${ipBanRecords} ADD COLUMN `silent` TINYINT(1);
ALTER TABLE ${ipMutes} ADD COLUMN `silent` TINYINT(1);
ALTER TABLE ${ipMuteRecords} ADD COLUMN `silent` TINYINT(1);
ALTER TABLE ${ipRangeBans} ADD COLUMN `silent` TINYINT(1);
ALTER TABLE ${ipRangeBanRecords} ADD COLUMN `silent` TINYINT(1);
ALTER TABLE ${nameBans} ADD COLUMN `silent` TINYINT(1);

-- Soft mute
ALTER TABLE ${playerMutes} ADD COLUMN `soft` TINYINT(1), ADD KEY `${playerMutes}_soft_idx` (`soft`);
ALTER TABLE ${playerMuteRecords} ADD COLUMN `createdReason` VARCHAR(255), ADD COLUMN `soft` TINYINT(1), ADD KEY `${playerMuteRecords}_soft_idx` (`soft`);

-- Created reason
ALTER TABLE ${playerBanRecords} ADD COLUMN `createdReason` VARCHAR(255);
ALTER TABLE ${ipBanRecords} ADD COLUMN `createdReason` VARCHAR(255);
ALTER TABLE ${ipRangeBanRecords} ADD COLUMN `createdReason` VARCHAR(255);

-- Mute unique key
ALTER TABLE ${playerMutes} ADD UNIQUE KEY `${playerMutes}_player_idx` (`player_id`);

-- Warn points/expires
ALTER TABLE ${playerWarnings} ADD COLUMN `expires` INT(10) NOT NULL DEFAULT 0, ADD KEY `${playerWarnings}_expires_idx` (`expires`);
ALTER TABLE ${playerWarnings} ADD COLUMN `points` INT(10) NOT NULL DEFAULT 1, ADD KEY `${playerWarnings}_points_idx` (`points`);
ALTER TABLE ${playerWarnings} MODIFY COLUMN `points` DECIMAL(60,2) NOT NULL DEFAULT 1;

-- Report workflow columns
ALTER TABLE ${playerReports} ADD COLUMN `state_id` INT(11) NOT NULL DEFAULT 1, ADD COLUMN `assignee_id` BINARY(16), ADD KEY `${playerReports}_state_id_idx` (`state_id`), ADD KEY `${playerReports}_assignee_id_idx` (`assignee_id`);
ALTER TABLE ${playerReports} MODIFY assignee_id BINARY(16) NULL;

-- Online mute
ALTER TABLE ${playerMutes} ADD COLUMN `onlineOnly` TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE ${playerMutes} ADD COLUMN `pausedRemaining` BIGINT UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE ${playerMuteRecords} ADD COLUMN `onlineOnly` TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE ${playerMuteRecords} ADD COLUMN `remainingOnlineTime` BIGINT UNSIGNED NOT NULL DEFAULT 0;

-- History
ALTER TABLE ${playerHistory} MODIFY `ip` VARBINARY(16) NULL;
ALTER TABLE ${playerHistory} ADD COLUMN `name` VARCHAR(16) NOT NULL DEFAULT '' AFTER `player_id`;
CREATE INDEX idx_playerhistory_name ON ${playerHistory} (name);
