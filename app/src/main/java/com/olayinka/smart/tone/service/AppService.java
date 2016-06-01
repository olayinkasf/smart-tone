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

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.activity.CollectionEditActivity;
import com.olayinka.smart.tone.activity.MediaGroupActivity;
import com.olayinka.smart.tone.model.Media;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 5/3/2015.
 */
public class AppService extends IntentService {
    public static final String SAVE_COLLECTION = "save.collection";
    public static final String FAUX_ID = "faux.id";
    private static final String NAME = "SmartTone App Service";
    public static final String EXPORT_COLLECTIONS = "export.collections";
    public static final String SAVE_FOLDER_COLLECTION = "save.folder.collection";
    public static final String EXTRA_FOLDER = "extra.folder";


    public AppService(String name) {
        super(name);
    }

    public AppService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getType()) {
            case SAVE_COLLECTION: {
                String selectionString = intent.getStringExtra(MediaGroupActivity.SELECTION);
                String collectionName = intent.getStringExtra(CollectionEditActivity.COLLECTION_NAME);
                long collectionId = intent.getLongExtra(CollectionEditActivity.COLLECTION_ID, 0);
                long fauxId = intent.getLongExtra(FAUX_ID, 0);
                try {
                    Media.saveCollection(getApplicationContext(), collectionId, collectionName, selectionString, -1);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                intent = new Intent(SAVE_COLLECTION);
                intent.putExtra(FAUX_ID, fauxId);
                sendBroadcast(intent);
                break;
            }
            case EXPORT_COLLECTIONS:
                try {
                    export();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case SAVE_FOLDER_COLLECTION: {
                long folderId = intent.getLongExtra(EXTRA_FOLDER, -1);
                if (folderId == -1) throw new RuntimeException("Haha! Joke is on you.");
                String collectionName = intent.getStringExtra(CollectionEditActivity.COLLECTION_NAME);
                long fauxId = intent.getLongExtra(FAUX_ID, 0);
                try {
                    Media.saveCollection(getApplicationContext(), 0, collectionName, tonesInFolder(this, folderId), folderId);
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                intent = new Intent(SAVE_FOLDER_COLLECTION);
                intent.putExtra(FAUX_ID, fauxId);
                sendBroadcast(intent);
                break;
            }
        }
    }

    public static String tonesInFolder(Context context, long folderId) {
        SQLiteDatabase database = AppSqlHelper.instance(context).getReadableDatabase();
        Cursor cursor = database.query(
                Media.TABLE, new String[]{Media.Columns._ID, Media.Columns.NAME},
                Media.Columns.FOLDER_ID + Media.EQUALS, new String[]{String.valueOf(folderId)}, null, null, Media.Columns.NAME
        );
        JSONArray jsonArray = new JSONArray();
        while (cursor.moveToNext()) jsonArray.put(cursor.getLong(0));
        cursor.close();
        return jsonArray.toString();
    }


    public String export() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String fileName = "backup-" + new SimpleDateFormat("dd-MM-yyyy.HH:mm.SSS").format(new Date()) + ".smt";
        File file = Utils.getExternalStorageDir("export", fileName);

        if (file == null) {
            Utils.notify(this, getString(R.string.cant_create_dir), R.id.backup, null);
            return null;
        }

        FileWriter writer = new FileWriter(file);

        try {
            SQLiteDatabase database = AppSqlHelper.instance(getApplicationContext()).getReadableDatabase();
            Cursor collectionCursor = database.rawQuery("select * from collection where folder_id = -1", null);
            if (collectionCursor.getCount() == 0) return null;
            JSONArray collections = new JSONArray();
            while (collectionCursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Media.CollectionColumns.NAME, collectionCursor.getString(1));
                JSONArray tones = new JSONArray();
                Cursor cursor = database.rawQuery(
                        "select m.path from media m inner join tone t  on m._id = t.media_id  where t.collection_id = ?",
                        new String[]{"" + collectionCursor.getLong(0)}
                );
                while (cursor.moveToNext()) {
                    JSONObject wrapper = new JSONObject();
                    wrapper.put(Media.Columns.PATH, cursor.getString(0));
                    tones.put(wrapper);
                }
                jsonObject.put("tones", tones);
                collections.put(jsonObject);
                cursor.close();
            }
            collectionCursor.close();
            writer.write(collections.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        writer.flush();
        writer.close();

        Uri selectedUri = Uri.parse(file.getParent());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");
        if (intent.resolveActivity(getPackageManager()) != null)
            Utils.notify(this, getString(R.string.export_complete) + " " + file.getAbsolutePath(), R.id.backup, PendingIntent.getActivity(this, 0, intent, 0));
        else
            Utils.notify(this, getString(R.string.export_complete) + " " + file.getAbsolutePath(), R.id.backup, null);
        return file.getAbsolutePath();
    }


}
