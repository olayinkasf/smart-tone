/*
 * Copyright 2015
 *
 * Olayinka S. Folorunso <mail@olayinkasf.com>
 * http://olayinkasf.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.olayinka.smart.tone.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.olayinka.smart.tone.AppSqlHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Olayinka on 4/26/2015.
 */
public class Media {


    public static final String TABLE = "media";
    public static final String ALBUM = "album";
    public static final String AND = " AND ";
    public static final String EQUALS = " = ? ";
    public static final String NOT_EQUALS = " != ? ";
    public static final String NOT_IN = " NOT IN ";
    public static final String OR = " OR ";
    public static String LIKE = " LIKE ?";


    public static JSONArray getTones(Context context, long mCollectionId) throws JSONException {
        SQLiteDatabase database = AppSqlHelper.instance(context).getReadableDatabase();
        String[] columns = new String[]{ToneColumns.MEDIA_ID};
        Cursor cursor = database.query(
                Tone.TABLE,
                columns,
                ToneColumns.COLLECTION_ID + Media.EQUALS,
                new String[]{"" + mCollectionId},
                null, null, null
        );
        JSONArray tones = new JSONArray();
        while (cursor.moveToNext()) {
            tones.put(cursor.getString(0));
        }
        cursor.close();
        return tones;
    }

    public static JSONObject getCollection(Context context, long mCollectionId) throws JSONException {
        SQLiteDatabase database = AppSqlHelper.instance(context).getReadableDatabase();
        JSONObject object = new JSONObject();
        Cursor cursor = database.query(
                Collection.TABLE,
                new String[]{"*"},
                CollectionColumns._ID + Media.EQUALS,
                new String[]{"" + mCollectionId},
                null, null, null
        );
        if (cursor.moveToNext()) {
            object.put(CollectionColumns._ID, cursor.getLong(0));
            object.put(CollectionColumns.NAME, cursor.getString(1));
            object.put(CollectionColumns.DATE_CREATED, cursor.getString(2));
        }
        cursor.close();

        return object;
    }

    public static JSONObject getMedia(Context context, long mediaId) throws JSONException {
        SQLiteDatabase database = AppSqlHelper.instance(context).getReadableDatabase();
        JSONObject object = new JSONObject();
        Cursor cursor = database.query(
                Media.TABLE, new String[]{"*"},
                Columns._ID + Media.EQUALS, new String[]{"" + mediaId},
                null, null, null
        );
        if (cursor.moveToNext()) {
            String[] columns = cursor.getColumnNames();
            for (int i = 0; i < columns.length; i++)
                object.put(columns[i], cursor.getString(i));
        }
        cursor.close();

        return object;
    }

    public static void saveCollection(Context applicationContext, long collectionId, String collectionName, String selectionString) throws JSONException {
        SQLiteDatabase database = AppSqlHelper.instance(applicationContext).getWritableDatabase();
        database.beginTransaction();
        ContentValues collectionValues = new ContentValues();
        collectionValues.put(Media.CollectionColumns.NAME, collectionName);
        if (collectionId > 0) collectionValues.put(Media.CollectionColumns._ID, collectionId);
        try {
            collectionId = database.insertOrThrow(Media.Collection.TABLE, null, collectionValues);
        } catch (SQLException e) {
            int affected = database.update(Media.Collection.TABLE, collectionValues, Media.CollectionColumns._ID + Media.EQUALS, new String[]{"" + collectionId});
            if (affected > 1) {
                throw new RuntimeException();
            }
        }
        database.delete(Tone.TABLE, ToneColumns.COLLECTION_ID + EQUALS, new String[]{"" + collectionId});
        JSONArray selection = new JSONArray(selectionString);
        ContentValues toneValues = new ContentValues();
        toneValues.put(Media.ToneColumns.COLLECTION_ID, collectionId);
        for (int i = 0; i < selection.length(); i++) {
            toneValues.put(Media.ToneColumns.MEDIA_ID, selection.getLong(i));
            try {
                database.insertOrThrow(Media.Tone.TABLE, null, toneValues);
            } catch (SQLException ignored) {
            }
        }
        database.setTransactionSuccessful();
        database.endTransaction();

    }

    public static void deleteCollection(Context context, long collectionId) {
        SQLiteDatabase database = AppSqlHelper.instance(context.getApplicationContext()).getWritableDatabase();
        database.beginTransaction();

        database.delete(Collection.TABLE, CollectionColumns._ID + EQUALS, new String[]{"" + collectionId});

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public static class Columns {
        public static final String _ID = "_id";
        public static final String NAME = "_name";
        public static final String PATH = "path";
        public static final String ALBUM_ID = ALBUM + _ID;
        public static final String ALBUM_NAME = ALBUM + NAME;
        public static final String ARTIST_NAME = "artist" + NAME;
        public static final String FOLDER_ID = "folder" + _ID;
        public static final String IS_RINGTONE = "is_ringtone";
        public static final String IS_NOTIFICATION = "is_notification";
        public static final String IS_INTERNAL = "is_internal";
        public static final String MEDIA_ID = "media" + Columns._ID;
    }

    public static class Tone {
        public static final String TABLE = "tone";
    }

    public static class ToneColumns {
        public static final String _ID = Columns._ID;
        public static final String MEDIA_ID = Columns.MEDIA_ID;
        public static final String COLLECTION_ID = "collection" + CollectionColumns._ID;
    }

    public static class Album {

        public static final String TABLE = "album";

    }

    public static class AlbumColumns {

        public static final String _ID = Columns._ID;
        public static final String ARTIST_NAME = "artist" + Columns.NAME;
        public static final String NAME = Columns.NAME;
        public static final String IS_INTERNAL = Columns.IS_INTERNAL;
        public static final String ALBUM_ID = Columns.ALBUM_ID;
    }

    public static class Folder {

        public static final String TABLE = "folder";


    }

    public static class FolderColumns {
        public static final String _ID = "_id";
        public static final String PATH = Columns.PATH;
        public static final String NAME = Columns.NAME;
        public static final String IS_INTERNAL = Columns.IS_INTERNAL;
        public static final String ALBUM_ID = Columns.ALBUM_ID;
    }

    public static class Collection {
        public static final String TABLE = "collection";
    }

    static public class CollectionColumns {
        public static final String _ID = "_id";
        public static final String NAME = Columns.NAME;
        public static final String DATE_CREATED = "date_created";
    }
}
