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

package com.olayinka.smart.tone.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;

import com.olayinka.smart.tone.AppLogger;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.model.Media;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 4/20/2015.
 */
public class IndexerService extends IntentService {

    public static final String MSG_DONE = "indexer.done";
    public static final String[] PROJECTION = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.IS_NOTIFICATION,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.DURATION,
    };
    public static final String SELECTION = MediaStore.Audio.Media.IS_MUSIC + " != 0 OR "
            + MediaStore.Audio.Media.IS_NOTIFICATION + " != 0 OR "
            + MediaStore.Audio.Media.IS_RINGTONE + " != 0";
    private static final String NAME = "SmartTone Indexer Service";
    private static final String LAST_INDEX = "last.index";
    public static final String FORCE_INDEX = "force.index";

    public IndexerService(String name) {
        super(name);
    }

    public IndexerService() {
        super(NAME);
    }

    private void index() {
        SQLiteDatabase database = AppSqlHelper.instance(getApplicationContext()).getWritableDatabase();
        database.beginTransaction();

        List<ContentValues> tones = cacheAllTones(database);
        database.execSQL("delete from " + Media.TABLE);
        database.execSQL("delete from " + Media.Tone.TABLE);
        database.execSQL("delete from " + Media.Album.TABLE);
        indexUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, database, 0);
        indexUri(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, database, 1);
        updateFolderCollections(database, tones);
        saveCachedTones(database, tones);

        database.setTransactionSuccessful();
        database.endTransaction();

        getSharedPreferences(NAME, MODE_PRIVATE)
                .edit()
                .putLong(LAST_INDEX, System.currentTimeMillis())
                .apply();
    }

    private void updateFolderCollections(SQLiteDatabase database, List<ContentValues> tones) {
        Cursor cursor = database.query(
                Media.Collection.TABLE,
                new String[]{Media.CollectionColumns._ID, Media.CollectionColumns.FOLDER_ID},
                Media.CollectionColumns.FOLDER_ID + Media.NOT_EQUALS + -1, null, null, null, null
        );

        while (cursor.moveToNext()) {
            long sortOrder = 10000;
            Cursor mediaCursor = database.query(
                    Media.TABLE,
                    new String[]{Media.Columns._ID},
                    Media.Columns.FOLDER_ID + Media.EQUALS + cursor.getLong(1), null, null, null, null
            );
            while (mediaCursor.moveToNext()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Media.ToneColumns.COLLECTION_ID, cursor.getLong(0));
                contentValues.put(Media.ToneColumns.MEDIA_ID, mediaCursor.getLong(0));
                contentValues.put(Media.ToneColumns.SORT_ORDER, sortOrder++);
                tones.add(contentValues);
            }
            mediaCursor.close();
        }
        cursor.close();
    }

    private void saveCachedTones(SQLiteDatabase database, List<ContentValues> tones) {
        for (ContentValues toneValues : tones) {
            Cursor cursor = database.query(Media.TABLE, new String[]{Media.Columns._ID}, Media.Columns.PATH + Media.EQUALS, new String[]{toneValues.getAsString(Media.Columns.PATH)}, null, null, null);
            if (cursor.getCount() != 1) {
                cursor.close();
                continue;
            }
            cursor.moveToNext();
            toneValues.put(Media.ToneColumns.MEDIA_ID, cursor.getLong(0));
            toneValues.remove(Media.Columns.PATH);
            try {
                database.insertOrThrow(Media.Tone.TABLE, null, toneValues);
            } catch (SQLException ignored) {
                AppLogger.wtf(this, "saveCachedTones", ignored);
            }
            cursor.close();
        }
    }

    private List<ContentValues> cacheAllTones(SQLiteDatabase writableDatabase) {
        List<ContentValues> tones = new ArrayList<>(200);
        Cursor cursor = writableDatabase.rawQuery("SELECT  t.collection_id,  m.path, t.sort_order FROM tone t INNER JOIN media m ON t.media_id = m._id", null);
        while (cursor.moveToNext()) {
            ContentValues contentValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
            tones.add(contentValues);
        }
        cursor.close();
        return tones;
    }

    private void indexUri(Uri uri, SQLiteDatabase database, int location) {
        Cursor cursor = getApplicationContext().getContentResolver()
                .query(uri, PROJECTION, SELECTION, null, null);
        AppLogger.wtf(this, "indexUri/cursorCount", "" + cursor.getCount());
        while (cursor.moveToNext()) {
            ContentValues albumValues = new ContentValues();
            ContentValues folderValues = new ContentValues();
            ContentValues mediaValues = new ContentValues();

            mediaValues.put(Media.Columns.IS_INTERNAL, location);
            folderValues.put(Media.Columns.IS_INTERNAL, location);
            albumValues.put(Media.Columns.IS_INTERNAL, location);

            albumValues.put(Media.AlbumColumns.ALBUM_ID, cursor.getString(3));
            albumValues.put(Media.AlbumColumns.NAME, cursor.getString(4));
            albumValues.put(Media.AlbumColumns.ARTIST_NAME, cursor.getString(5));
            try {
                database.insertOrThrow(Media.Album.TABLE, null, albumValues);
            } catch (SQLException ignored) {
            }

            File file = new File(cursor.getString(2));
            file = file.getParentFile();
            folderValues.put(Media.FolderColumns.PATH, file.getAbsolutePath());
            folderValues.put(Media.FolderColumns.NAME, file.getName());
            folderValues.put(Media.FolderColumns.ALBUM_ID, cursor.getString(3));
            long folderId;
            try {
                folderId = database.insertOrThrow(Media.Folder.TABLE, null, folderValues);
            } catch (SQLException ignored) {
                folderId = Media.getFolderId(database, file.getAbsolutePath());
                if (folderId == -1) {
                    AppLogger.wtf(this, "indexUri", "This shouldn't happen.");
                    throw new RuntimeException();
                }
            }

            mediaValues.put(Media.Columns.MEDIA_ID, cursor.getString(0));
            mediaValues.put(Media.Columns.NAME, cursor.getString(1));
            mediaValues.put(Media.Columns.PATH, cursor.getString(2));
            mediaValues.put(Media.Columns.ALBUM_ID, cursor.getString(3));
            mediaValues.put(Media.Columns.ALBUM_NAME, cursor.getString(4));
            mediaValues.put(Media.Columns.ARTIST_NAME, cursor.getString(5));
            mediaValues.put(Media.Columns.IS_NOTIFICATION, 0);
            mediaValues.put(Media.Columns.IS_RINGTONE, 0);
            if (cursor.getInt(6) != 0 || cursor.getInt(9) < 30 * 1000 || cursor.getString(4).equals("ui")) {
                mediaValues.put(Media.Columns.IS_NOTIFICATION, 1);
            } else {
                mediaValues.put(Media.Columns.IS_RINGTONE, 1);
            }
            if (cursor.getInt(8) != 0) {
                mediaValues.put(Media.Columns.IS_RINGTONE, 1);
            }
            mediaValues.put(Media.Columns.FOLDER_ID, folderId);

            try {
                database.insertOrThrow(Media.TABLE, null, mediaValues);
            } catch (SQLException ignored) {
            }
        }
        cursor.close();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences prefs = getSharedPreferences(NAME, MODE_PRIVATE);
        long lastIndex = prefs.getLong(LAST_INDEX, System.currentTimeMillis() - getIndexFreq(prefs));
        boolean forceIndexFlag = intent.getBooleanExtra(FORCE_INDEX, false);
        AppLogger.wtf(this, "last.index", "" + lastIndex);
        AppLogger.wtf(this, "index.freq", "" + getIndexFreq(prefs));
        AppLogger.wtf(this, "force.index", "" + forceIndexFlag);
        if (System.currentTimeMillis() - lastIndex >= getIndexFreq(prefs) || forceIndexFlag)
            index();
        if (intent.getData() != null)
            importCollection(intent.getData());
        cleanCollection();
        intent = new Intent(MSG_DONE);
        sendBroadcast(intent);
        stopSelf();
    }

    private long getIndexFreq(SharedPreferences prefs) {
        return (new int[]{1, 6, 12, 24, 7 * 24}[prefs.getInt(AppSettings.INDEX_FREQ, 0)]) * 60 * 60 * 1000;
    }

    private void cleanCollection() {
        SQLiteDatabase database = AppSqlHelper.instance(getApplicationContext()).getWritableDatabase();
        database.beginTransaction();
        database.execSQL("DELETE FROM collection  WHERE NOT EXISTS(SELECT NULL FROM tone WHERE tone.collection_id =collection. _id)");
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    private void importCollection(Uri uri) {
        File file = new File(uri.getPath());
        try {
            SQLiteDatabase database = AppSqlHelper.instance(getApplicationContext()).getReadableDatabase();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String jsonString = br.readLine();
            if(jsonString == null) return;
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.optString(Media.CollectionColumns.NAME, jsonObject.optString("name"));
                long folderPath = jsonObject.optLong(Media.CollectionColumns.FOLDER_ID, -1);
                name = name + " " + getString(R.string.imported);
                JSONArray selection = jsonObject.getJSONArray("tones");
                ArrayList<Long> selectionIds = new ArrayList<>();
                for (int j = 0; j < selection.length(); j++) {
                    Cursor cursor = database.query(Media.TABLE, new String[]{Media.Columns._ID}, Media.Columns.PATH + Media.EQUALS, new String[]{selection.getJSONObject(j).getString(Media.Columns.PATH)}, null, null, null);

                    if (cursor.getCount() == 1) {
                        cursor.moveToNext();
                        selectionIds.add(cursor.getLong(0));
                    }
                    cursor.close();
                }

                if (selectionIds.size() != 0) {
                    Media.saveCollection(getApplicationContext(), 0, name, Utils.serialize(selectionIds), folderPath);
                }
            }
            database.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
