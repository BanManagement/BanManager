-- Timestamp columns to BIGINT UNSIGNED
ALTER TABLE ${playerBans} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `expires` `expires` BIGINT UNSIGNED;
ALTER TABLE ${playerUnbans} CHANGE `created` `created` BIGINT UNSIGNED;
ALTER TABLE ${playerMutes} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `expires` `expires` BIGINT UNSIGNED;
ALTER TABLE ${playerUnmutes} CHANGE `created` `created` BIGINT UNSIGNED;
ALTER TABLE ${playerNotes} CHANGE `created` `created` BIGINT UNSIGNED;
ALTER TABLE ${ipBans} CHANGE `created` `created` BIGINT UNSIGNED, CHANGE `expires` `expires` BIGINT UNSIGNED;
ALTER TABLE ${ipUnbans} CHANGE `created` `created` BIGINT UNSIGNED;

-- Soft mute (global mutes only)
ALTER TABLE ${playerMutes} ADD COLUMN `soft` TINYINT(1), ADD KEY `${playerMutes}_soft_idx` (`soft`);
