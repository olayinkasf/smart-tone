PRAGMA foreign_keys = OFF;

ALTER TABLE 'tone' ADD COLUMN sort_order INTEGER NOT NULL DEFAULT -1;

PRAGMA foreign_keys = ON;
