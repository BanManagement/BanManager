-- Sample table demonstrating MigrationService usage. Stores one row per
-- denied login captured by the sample plugin's PlayerDeniedEvent listener.
CREATE TABLE IF NOT EXISTS bm_sample_denied_logins (
  id        INT NOT NULL AUTO_INCREMENT,
  uuid      BINARY(16) NULL,
  name      VARCHAR(32) NOT NULL,
  reason    VARCHAR(64) NOT NULL,
  created   BIGINT NOT NULL,
  PRIMARY KEY (id),
  KEY idx_bm_sample_denied_logins_uuid (uuid),
  KEY idx_bm_sample_denied_logins_created (created)
);
