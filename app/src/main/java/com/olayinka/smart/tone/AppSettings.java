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

package com.olayinka.smart.tone;

import android.app.PendingIntent;
import android.content.*;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
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
    public static final String LAST_USED = ".last.used";
    public static final String GOT_IT_DOUBLE_TAP = "got.it.double.tap";
    public static final String GOT_IT_WHAT_IS_NEW = "got.it.what.is.new";
    public static final String JUST_CHANGED = "just.changed";
    public static final String ORDER_CHANGE = "order.change";
    public static final String INDEX_FREQ = "index.frequency";
    public static final String LOG_APP_ACTIVITY = "log.app.activity";
    public static final String ASK_LOG_APP_ACTIVITY = "ask.log.app.activity";
    public static final String BIND_ACCESSIBILITY_SERVICE = "android.permission.BIND_ACCESSIBILITY_SERVICE";
    public static final String BIND_NOTIFICATION_LISTENER_SERVICE = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE";
    public static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    public static final String NOTIFICATION_PERMISSION = (!Utils.hasJellyBeanMR2() ? AppSettings.BIND_ACCESSIBILITY_SERVICE : AppSettings.BIND_NOTIFICATION_LISTENER_SERVICE);
    public static final String NOTIF_CANCELED = "notif.canceled";
    public static final String GOT_IT_SAMSUNG = "got.it.samsung";


    public static void setFreq(Context context, String key, int which, int arrayId) {
        long time = (which == 3 ? 4 : which) * 6 * 60 * 60 * 1000;
        if (which == 0) time = 0;
        context.getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).edit().putLong(key, time).apply();
        context.getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).edit().putString(key + TEXT, context.getResources().getStringArray(arrayId)[which]).apply();
    }

    private static Uri changeSound(Context context, int type, String key, String freqKey, boolean isRingtone, boolean isNotification) throws JSONException {
        try {
            if (!Utils.checkWriteSettings(context)) {
                AppLogger.wtf(context, "changeSound", "No write settings permission");
                return null;
            }
        } catch (Throwable throwable) {
            Log.wtf("changeSound", throwable.getMessage());
            throwable.printStackTrace();
        }
        ContentValues contentValues = new ContentValues();

        Uri notifUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
        long notifId = notifUri != null ? ContentUris.parseId(notifUri) : -1L;
        long ringtoneId = ringtoneUri != null ? ContentUris.parseId(ringtoneUri) : -1L;
        AppLogger.wtf(context, "changeSound/currentNotifSound", "" + notifId);
        AppLogger.wtf(context, "changeSound/currentRingtoneSound", "" + ringtoneId);
        contentValues.put(MediaStore.Audio.Media.IS_NOTIFICATION, 0);
        contentValues.put(MediaStore.Audio.Media.IS_RINGTONE, 0);
        context.getContentResolver().update(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                contentValues,
                MediaStore.Audio.Media.IS_MUSIC + Media.EQUALS + Media.AND + MediaStore.Audio.Media._ID + Media.NOT_IN + "(?,?)",
                new String[]{"1", "" + notifId, "" + ringtoneId}
        );
        context.getContentResolver().update(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                contentValues,
                MediaStore.Audio.Media.IS_MUSIC + Media.EQUALS + Media.AND + MediaStore.Audio.Media._ID + Media.NOT_IN + "(?,?)",
                new String[]{"1", "" + notifId, "" + ringtoneId}
        );


        long collectionId = context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)
                .getLong(key, 0L);
        if (collectionId == 0) {
            AppLogger.wtf(context, "changeSound", "No active collection for " + key);
            return null;
        }
        JSONArray tones = Media.getTones(context, collectionId);
        if (tones.length() == 0) {
            AppLogger.wtf(context, "changeSound", "No tones for collection for " + key);
            context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit()
                    .putLong(key, 0).apply();
            return null;
        }

        int position;
        int lastIndex = context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).getInt(key + LAST_USED, -1);
        if (!context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).getBoolean(ORDER_CHANGE, false)) {
            position = new Random().nextInt(tones.length());
            if (position == lastIndex)
                position = (lastIndex + 1) % tones.length();
        } else {
            lastIndex = Math.min(lastIndex, tones.length() - 1);
            position = (lastIndex + 1) % tones.length();
        }
        context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit().putInt(key + LAST_USED, position).apply();
        AppLogger.wtf(context, "changeSound/" + key, "" + position);
        JSONObject tone = Media.getMedia(context, tones.getLong(position));
        Uri uri;
        if (tone.getInt(Media.Columns.IS_INTERNAL) == 1) uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        else uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        uri = Uri.withAppendedPath(uri, "" + tone.getLong(Media.Columns.MEDIA_ID));
        if (Utils.isValidUri(context, uri)) {
            {
                ContentValues values = new ContentValues();
                if (isRingtone)
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                if (isNotification)
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                int updated = context.getContentResolver().update(uri, values, null, null);
                if (updated > 0) {
                    AppLogger.wtf(context, "changeSound/" + type, "New sound added to list");
                }
            }
            RingtoneManager.setActualDefaultRingtoneUri(context, type, uri);
            context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit()
                    .putLong(freqKey + LAST_CHANGE, System.currentTimeMillis()).apply();
        } else {
            AppLogger.wtf(context, "changeSound/", "Invalid uri selected " + uri.toString());
            uri = null;
        }
        return uri;
    }

    public static Uri changeRingtone(Context context) throws JSONException {
        return changeRingtone(context, true);
    }

    public static Uri changeRingtone(Context context, Boolean notify) throws JSONException {
        Uri uri = changeSound(context, RingtoneManager.TYPE_RINGTONE, ACTIVE_RINGTONE, RINGTONE_FREQ, true, false);
        if (uri != null && notify) {
            notify(context, context.getString(R.string.ringtone_change), R.id.changeRingtoneNotif);
        }
        if (uri != null) {
            Intent intent = new Intent(JUST_CHANGED);
            intent.putExtra(JUST_CHANGED, ACTIVE_RINGTONE);
            context.sendBroadcast(intent);
        }
        return uri;
    }

    public static Uri changeNotificationSound(Context context) throws JSONException {
        return changeNotificationSound(context, true);
    }

    public static Uri changeNotificationSound(Context context, Boolean notify) throws JSONException {
        AppLogger.wtf(context, "changeNotificationSound", notify.toString());
        Uri uri = changeSound(context, RingtoneManager.TYPE_NOTIFICATION, ACTIVE_NOTIFICATION, NOTIF_FREQ, false, true);
        if (uri != null && notify) {
            notify(context, context.getString(R.string.notification_change), R.id.changeNotifNotif);
        }

        if (uri != null) {
            Intent intent = new Intent(JUST_CHANGED);
            intent.putExtra(JUST_CHANGED, ACTIVE_NOTIFICATION);
            context.sendBroadcast(intent);
        }
        AppLogger.wtf(context, "changeNotificationSound/uri: ", String.valueOf(uri));
        return uri;
    }

    private static void notify(Context context, String string, int notificationId) {
        SharedPreferences preferences = context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        if (preferences.getBoolean(NOTIFY_CHANGE, false)) {
            Intent intent = new Intent(Settings.ACTION_SOUND_SETTINGS);
            Utils.notify(context, string, notificationId, PendingIntent.getActivity(context, 0, intent, 0));
        }
    }

    public static void deleteCheck(Context context, long collectionId) {
        SharedPreferences prefs = context.getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        if (prefs.getLong(AppSettings.ACTIVE_RINGTONE, 0) == collectionId)
            prefs.edit().remove(ACTIVE_RINGTONE).apply();
        if (prefs.getLong(AppSettings.ACTIVE_NOTIFICATION, 0) == collectionId)
            prefs.edit().remove(ACTIVE_NOTIFICATION).apply();
    }

    public static Long[] getActivePairs(Context context) {
        Long[] ret = new Long[]{0L, 0L};
        SharedPreferences prefs = context.getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        ret[0] = prefs.getLong(AppSettings.ACTIVE_RINGTONE, 0);
        ret[1] = prefs.getLong(AppSettings.ACTIVE_NOTIFICATION, 0);
        return ret;
    }
}
