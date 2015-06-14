package com.olayinka.smart.tone;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import com.olayinka.smart.tone.model.Media;
import lib.olayinka.smart.tone.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Olayinka on 5/8/2015.
 */
public class AppSettings {
    public static final String APP_SETTINGS = "app.settings";
    public static final String ACTIVE_NOTIFICATION = "active.notification";
    public static final String ACTIVE_RINGTONE = "active.ringtone";
    public static final String ACTIVE_APP_SERVICE = "active.app.service";
    public static final String TEXT = ".text";
    public static final String RINGTONE_FREQ = "ringtone.frequency";
    public static final String NOTIF_FREQ = "notification.frequency";
    public static final String NOTIFY_CHANGE = "notify.change";
    public static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    public static final String LAST_CHANGE = ".last.change";
    public static final String FORCE_CHANGE_NOTIF = "force.change.notif";
    public static final String FORCE_CHANGE_RINGTONE = "force.change.notification";
    public static final String GOT_IT_DOUBLE_TAP = "got.it.double.tap";
    public static final String GOT_IT_LONG_PRESS = "got.it.long.press";

    public static void setFreq(Context context, String key, int which, int arrayId) {
        long time = (which == 3 ? 4 : which) * 6 * 60 * 60 * 1000;
        if (which == 0) time = 0;
        context.getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).edit().putLong(key, time).apply();
        context.getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).edit().putString(key + TEXT, context.getResources().getStringArray(arrayId)[which]).apply();
    }

    private static Uri changeSound(Context context, int type, String key, String freqKey, Boolean notify) throws JSONException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.Media.IS_NOTIFICATION, 0);
        contentValues.put(MediaStore.Audio.Media.IS_RINGTONE, 0);
        context.getContentResolver().update(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                contentValues,
                MediaStore.Audio.Media.IS_MUSIC + Media.EQUALS,
                new String[]{"1"}
        );
        context.getContentResolver().update(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                contentValues,
                MediaStore.Audio.Media.IS_MUSIC + Media.EQUALS,
                new String[]{"1"}
        );

        long collectionId = context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)
                .getLong(key, 0L);
        if (collectionId == 0) return null;
        JSONArray tones = Media.getTones(context, collectionId);
        if (tones.length() == 0) {
            context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit()
                    .putLong(key, 0).apply();
            return null;
        }
        int position = new Random().nextInt(tones.length());
        Log.wtf("changeSound/" + key, "" + position);
        JSONObject tone = Media.getMedia(context, tones.getLong(position));
        Uri uri = null;
        if (tone.getInt(Media.Columns.IS_INTERNAL) == 1) uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        else uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        uri = Uri.withAppendedPath(uri, "" + tone.getLong(Media.Columns.MEDIA_ID));
        if (Utils.isValidUri(context, uri)) {
            RingtoneManager.setActualDefaultRingtoneUri(context, type, uri);
            context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit()
                    .putLong(freqKey + LAST_CHANGE, System.currentTimeMillis()).apply();
        } else uri = null;
        return uri;
    }

    public static Uri changeNotificationSound(Context context) throws JSONException {
        return changeNotificationSound(context, true);
    }

    public static Uri changeRingtoneSound(Context context) throws JSONException {
        return changeRingtoneSound(context, true);
    }

    public static Uri changeNotificationSound(Context context, Boolean notify) throws JSONException {
        Uri uri = changeSound(context, RingtoneManager.TYPE_NOTIFICATION, ACTIVE_NOTIFICATION, NOTIF_FREQ, false);
        if (uri != null && notify) {
            notify(context, context.getString(R.string.notification_change));
        }
        return uri;
    }

    private static void notify(Context context, String string) {
        SharedPreferences preferences = context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        if (preferences.getBoolean(NOTIFY_CHANGE, false)) {
            Utils.notify(context, string);
        }
    }

    public static Uri changeRingtoneSound(Context context, Boolean notify) throws JSONException {
        Uri uri = changeSound(context, RingtoneManager.TYPE_RINGTONE, ACTIVE_RINGTONE, RINGTONE_FREQ, false);
        if (uri != null && notify) {
            notify(context, context.getString(R.string.ringtone_change));
        }
        return uri;
    }

    public static void deleteCheck(Context context, long collectionId) {
        SharedPreferences prefs = context.getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        if (prefs.getLong(AppSettings.ACTIVE_RINGTONE, 0) == collectionId)
            prefs.edit().remove(ACTIVE_RINGTONE).apply();
        if (prefs.getLong(AppSettings.ACTIVE_NOTIFICATION, 0) == collectionId)
            prefs.edit().remove(ACTIVE_NOTIFICATION).apply();
    }
}
