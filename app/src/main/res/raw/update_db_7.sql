PRAGMA foreign_keys = OFF;

CREATE TABLE IF NOT EXISTS `collection_` (
  `_id`          INTEGER PRIMARY KEY AUTOINCREMENT,
  `_name`        VARCHAR(255) NOT NULL,
  `date_created` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO 'collection_' SELECT * FROM 'collection';
DROP TABLE 'collection';
ALTER TABLE 'collection_' RENAME TO 'collection';

PRAGMA foreign_keys = ON;
