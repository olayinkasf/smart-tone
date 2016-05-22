PRAGMA foreign_keys = OFF;

ALTER TABLE 'collection' ADD COLUMN folder_id  INTEGER NOT NULL DEFAULT -1,

PRAGMA foreign_keys = ON;
