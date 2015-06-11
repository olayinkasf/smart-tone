package com.olayinka.smart.tone.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.activity.CollectionEditActivity;
import com.olayinka.smart.tone.activity.MediaGroupActivity;
import com.olayinka.smart.tone.model.Media;
import lib.olayinka.smart.tone.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Olayinka on 5/3/2015.
 */
public class AppService extends IntentService {
    public static final String SAVE_COLLECTION = "save.collection";
    public static final String FAUX_ID = "faux.id";
    private static final String NAME = "SmartTone App Service";
    public static final String EXPORT_COLLECTIONS = "export.collections";


    public AppService(String name) {
        super(name);
    }

    public AppService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getType()) {
            case SAVE_COLLECTION:
                String selectionString = intent.getStringExtra(MediaGroupActivity.SELECTION);
                String collectionName = intent.getStringExtra(CollectionEditActivity.COLLECTION_NAME);
                long collectionId = intent.getLongExtra(CollectionEditActivity.COLLECTION_ID, 0);
                long fauxId = intent.getLongExtra(FAUX_ID, 0);
                try {
                    Media.saveCollection(getApplicationContext(), collectionId, collectionName, selectionString);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                intent = new Intent(SAVE_COLLECTION);
                intent.putExtra(FAUX_ID, fauxId);
                sendBroadcast(intent);
                break;
            case EXPORT_COLLECTIONS:
                try {
                    export();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }


    public String export() throws IOException {
        File file;
        if (isExternalStorageWritable()) {
            File sdCard = Environment.getExternalStorageDirectory();
            file = new File(sdCard.getAbsolutePath() + "/com.olayinka.smart.tone/export");
        } else file = getDir("export", MODE_PRIVATE);
        if (!file.exists() && !file.mkdirs()) {
            Utils.notify(this, getString(R.string.cant_create_dir));
            return null;
        }

        @SuppressLint("SimpleDateFormat")
        String fileName = "backup-" + new SimpleDateFormat("dd-MM-yyyy.HH:mm.SSS").format(new Date()) + ".smt";

        file = new File(file, fileName);

        FileWriter writer = new FileWriter(file);

        try {
            SQLiteDatabase database = AppSqlHelper.instance(getApplicationContext()).getReadableDatabase();
            Cursor collectionCursor = database.rawQuery("select * from collection", null);
            JSONArray collections = new JSONArray();
            while (collectionCursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", collectionCursor.getString(1));
                JSONArray tones = new JSONArray();
                Cursor cursor = database.rawQuery(
                        "select m.path from media m inner join tone t  on m._id = t.media_id  where t.collection_id = ?",
                        new String[]{"" + collectionCursor.getLong(0)}
                );
                while (cursor.moveToNext()) {
                    JSONObject wrapper = new JSONObject();
                    wrapper.put("path", cursor.getString(0));
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
        Utils.notify(this, getString(R.string.export_complete) + " " + file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


}
