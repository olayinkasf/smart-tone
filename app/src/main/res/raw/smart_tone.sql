CREATE TABLE `album` (
  _id           INTEGER PRIMARY KEY AUTOINCREMENT,
  album_id      INTEGER,
  _name         VARCHAR(255) COLLATE NOCASE,
  artist_name   VARCHAR(255) COLLATE NOCASE,
  'is_internal' INTEGER(1),
  UNIQUE (`album_id`, `is_internal`)
);

CREATE TABLE `folder` (
  _id           INTEGER PRIMARY KEY AUTOINCREMENT,
  `album_id`    INTEGER,
  _name         VARCHAR(255) COLLATE NOCASE,
  path          VARCHAR(255) COLLATE NOCASE UNIQUE NOT NULL,
  'is_internal' INTEGER(1)
);

CREATE TABLE IF NOT EXISTS `media` (
  `_id`             INTEGER PRIMARY KEY AUTOINCREMENT,
  'media_id'        INTEGER                            NOT NULL,
  `_name`           VARCHAR(255) COLLATE NOCASE        NOT NULL,
  `path`            VARCHAR(255) COLLATE NOCASE UNIQUE NOT NULL,
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

CREATE TABLE IF NOT EXISTS `collection` (
  `_id`          INTEGER PRIMARY KEY AUTOINCREMENT,
  `_name`        VARCHAR(255) COLLATE NOCASE NOT NULL,
  `date_created` timestamp                   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  folder_id      INTEGER NOT NULL DEFAULT -1
);

CREATE TABLE IF NOT EXISTS `tone` (
  '_id'           INTEGER PRIMARY KEY AUTOINCREMENT,
  `media_id`      INTEGER,
  `collection_id` INTEGER NOT NULL,
  sort_order      INTEGER NOT NULL DEFAULT -1,
  UNIQUE (`media_id`, `collection_id`),
  FOREIGN KEY (`collection_id`) REFERENCES `collection` (`_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  FOREIGN KEY (`media_id`) REFERENCES `media` (`_id`) ON DELETE CASCADE ON UPDATE NO ACTION
);
