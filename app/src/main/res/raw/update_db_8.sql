PRAGMA foreign_keys = OFF;

CREATE TABLE `album_` (
  _id           INTEGER PRIMARY KEY AUTOINCREMENT,
  album_id      INTEGER,
  _name         VARCHAR(255) COLLATE NOCASE,
  artist_name   VARCHAR(255) COLLATE NOCASE,
  'is_internal' INTEGER(1),
  UNIQUE (`album_id`, `is_internal`)
);

CREATE TABLE `folder_` (
  _id           INTEGER PRIMARY KEY AUTOINCREMENT,
  `album_id`    INTEGER,
  _name         VARCHAR(255) COLLATE NOCASE,
  path          VARCHAR(255) UNIQUE NOT NULL,
  'is_internal' INTEGER(1)
);

CREATE TABLE IF NOT EXISTS `media_` (
  `_id`             INTEGER PRIMARY KEY AUTOINCREMENT,
  'media_id'        INTEGER   NOT NULL,
  `_name`           VARCHAR(255) COLLATE NOCASE     NOT NULL,
  `path`            VARCHAR(255) UNIQUE             NOT NULL,
  `album_id`        INTEGER,
  `album_name`      VARCHAR(255) COLLATE NOCASE,
  `artist_name`     VARCHAR(255) COLLATE NOCASE,
  'folder_id'       INTEGER,
  'is_notification' INTEGER(1),
  'is_ringtone'     INTEGER(1),
  'is_internal'     INTEGER(1),
  UNIQUE (media_id, is_internal),
  FOREIGN KEY (`album_id`, is_internal) REFERENCES `album` (`album_id`, is_internal) ON DELETE NO ACTION ON UPDATE NO ACTION,
  FOREIGN KEY (`folder_id`) REFERENCES `folder` (`_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS `collection_` (
  `_id`          INTEGER PRIMARY KEY AUTOINCREMENT,
  `_name`        VARCHAR(255) NOT NULL COLLATE NOCASE,
  `date_created` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO 'album_' SELECT * FROM 'album';
INSERT INTO 'folder_' SELECT * FROM 'folder';
INSERT INTO 'media_' SELECT * FROM 'media';
INSERT INTO 'collection_' SELECT * FROM 'collection';

DROP TABLE 'album';
ALTER TABLE 'album_' RENAME TO 'album';
DROP TABLE 'folder';
ALTER TABLE 'folder_' RENAME TO 'folder';
DROP TABLE 'media';
ALTER TABLE 'media_' RENAME TO 'media';
DROP TABLE 'collection';
ALTER TABLE 'collection_' RENAME TO 'collection';

PRAGMA foreign_keys = ON;
