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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.service.MediaPlayerStub;

import java.io.File;
import java.util.Random;

import lib.olayinka.smart.tone.R;

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
    public static final String GOT_IT_SAMSUNG = "got.it.samsung";
    public static final String GOT_IT_HELP_PAGE = "got.it.help.page";


    public static final class ChangeParams implements Parcelable {
        private final int type;
        private final String mediaType;
        private final int notifId;
        private final int notifMessage;
        private final String logTag;
        private final String idPrefsName;
        private final String freqPrefsName;

        protected ChangeParams(int type, String mediaType, int notifId, int notifMessage, String logTag, String idPrefsName, String freqPrefsName) {
            this.type = type;
            this.mediaType = mediaType;
            this.notifId = notifId;
            this.notifMessage = notifMessage;
            this.logTag = logTag;
            this.idPrefsName = idPrefsName;
            this.freqPrefsName = freqPrefsName;
        }

        protected ChangeParams(Parcel in) {
            type = in.readInt();
            mediaType = in.readString();
            notifId = in.readInt();
            notifMessage = in.readInt();
            logTag = in.readString();
            idPrefsName = in.readString();
            freqPrefsName = in.readString();
        }

        public static final Creator<ChangeParams> CREATOR = new Creator<ChangeParams>() {
            @Override
            public ChangeParams createFromParcel(Parcel in) {
                return new ChangeParams(in);
            }

            @Override
            public ChangeParams[] newArray(int size) {
                return new ChangeParams[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(type);
            dest.writeString(mediaType);
            dest.writeInt(notifId);
            dest.writeInt(notifMessage);
            dest.writeString(logTag);
            dest.writeString(idPrefsName);
            dest.writeString(freqPrefsName);
        }

        public String getIdPrefsName() {
            return idPrefsName;
        }

        public int getNotifMessage() {
            return notifMessage;
        }

        public int getNotifId() {
            return notifId;
        }

        public int getType() {
            return type;
        }

    }

    private static final ChangeParams NOTIF_CHANGE_PARAMS = new ChangeParams(
            RingtoneManager.TYPE_NOTIFICATION,
            MediaStore.Audio.Media.IS_NOTIFICATION,
            R.id.changeNotifNotif,
            R.string.notification_change,
            "changeNotificationSound",
            ACTIVE_NOTIFICATION,
            NOTIF_FREQ
    );

    private static final ChangeParams RINGTONE_CHANGE_PARAMS = new ChangeParams(
            RingtoneManager.TYPE_RINGTONE,
            MediaStore.Audio.Media.IS_RINGTONE,
            R.id.changeRingtoneNotif,
            R.string.ringtone_change,
            "changeRingtone",
            ACTIVE_RINGTONE,
            RINGTONE_FREQ
    );

    public static void setFreq(Context context, String key, int which, int arrayId) {
        long time = (which == 3 ? 4 : which) * 6 * 60 * 60 * 1000;
        if (which == 0) time = 0;
        context.getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).edit().putLong(key, time).apply();
        context.getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).edit().putString(key + TEXT, context.getResources().getStringArray(arrayId)[which]).apply();
    }

    private static ContentValues changeSound(Context context, ChangeParams changeParams) {
        if (!checkWriteSettingsForChange(context)) return null;
        Uri previousUri = RingtoneManager.getActualDefaultRingtoneUri(context, changeParams.type);
        long collectionId = context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).getLong(changeParams.idPrefsName, 0L);
        if (collectionId == 0) {
            AppLogger.wtf(context, "changeSound", "No active collection for " + changeParams.idPrefsName);
            return null;
        }
        long[] tones = Media.getTones(context, collectionId);
        if (tones.length == 0) {
            AppLogger.wtf(context, "changeSound", "No tones for collection for " + changeParams.idPrefsName);
            context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit()
                    .putLong(changeParams.idPrefsName, 0).apply();
            return null;
        }
        int position = getPosition(context, changeParams, tones);
        context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).edit().putInt(changeParams.idPrefsName + LAST_USED, position).apply();
        AppLogger.wtf(context, "changeSound/" + changeParams.idPrefsName, "" + position);
        ContentValues media = Media.getMedia(context, tones[position]);
        Uri nextUri = getToneUri(media);
        File nextFile = Utils.fileForUri(context, nextUri);
        if (nextFile != null && nextFile.exists()) {
            ContentValues values = new ContentValues();
            values.put(changeParams.mediaType, true);
            int updated = context.getContentResolver().update(nextUri, values, null, null);
            if (updated > 0)
                AppLogger.wtf(context, "changeSound/" + changeParams.mediaType, "New sound added to list");
            RingtoneManager.setActualDefaultRingtoneUri(context, changeParams.type, nextUri);
            context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE)
                    .edit().putLong(changeParams.freqPrefsName + LAST_CHANGE, System.currentTimeMillis())
                    .apply();
            updatePreviousSettings(previousUri, context, changeParams);
            return media;
        } else {
            AppLogger.wtf(context, "changeSound/", "Invalid uri selected " + nextUri.toString());
        }
        return null;
    }

    public static Uri getToneUri(ContentValues media) {
        Uri uri;
        if (media.getAsInteger(Media.Columns.IS_INTERNAL) == 1)
            uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        else uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return Uri.withAppendedPath(uri, "" + media.getAsLong(Media.Columns.MEDIA_ID));
    }

    private static int getPosition(Context context, ChangeParams changeParams, long[] tones) {
        int lastIndex = context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).getInt(changeParams.idPrefsName + LAST_USED, -1);
        if (!context.getSharedPreferences(APP_SETTINGS, MODE_PRIVATE).getBoolean(ORDER_CHANGE, false)) {
            int position = new Random().nextInt(tones.length);
            if (position != lastIndex)
                return position;
        }
        lastIndex = Math.min(lastIndex, tones.length - 1);
        return (lastIndex + 1) % tones.length;

    }

    private static void updatePreviousSettings(Uri activeUri, Context context, ChangeParams changeParams) {
        if (activeUri == null) return;
        ContentValues contentValues = new ContentValues();
        AppLogger.wtf(context, String.format("changeSound/current: %s", changeParams.logTag), "" + activeUri);
        contentValues.put(changeParams.mediaType, false);
        context.getContentResolver().update(activeUri, contentValues, null, null);
    }

    private static boolean checkWriteSettingsForChange(Context context) {
        try {
            if (!Utils.checkWriteSettings(context)) {
                AppLogger.wtf(context, "changeSound", "No write settings permission");
                return false;
            }
        } catch (Throwable throwable) {
            Log.wtf("changeSound", throwable.getMessage());
            AppLogger.wtf(context, "changeSound", throwable);
            throwable.printStackTrace();
        }
        return true;
    }

    public static ContentValues changeRingtone(Context context) {
        return changeRingtone(context, true);
    }

    public static ContentValues changeNotificationSound(Context context) {
        return changeNotificationSound(context, true);
    }

    public static ContentValues changeRingtone(Context context, boolean notify) {
        return changeSound(
                context,
                notify,
                RINGTONE_CHANGE_PARAMS
        );
    }

    public static ContentValues changeNotificationSound(Context context, boolean notify) {
        return changeSound(
                context,
                notify,
                NOTIF_CHANGE_PARAMS
        );
    }

    public static ContentValues changeSound(Context context, boolean notify, ChangeParams changeParams) {
        AppLogger.wtf(context, changeParams.logTag, String.valueOf(notify));
        ContentValues mediaContentValues = changeSound(context, changeParams);
        if (mediaContentValues != null) {
            if (notify)
                MediaPlayerStub.notify(context, changeParams, mediaContentValues);
            broadcastChange(context, changeParams.idPrefsName);
            AppLogger.wtf(context, String.format("%s/uri: ", changeParams.logTag), mediaContentValues.toString());
        } else AppLogger.wtf(context, String.format("%s/uri: ", changeParams.logTag), "failed");
        return mediaContentValues;
    }

    private static void broadcastChange(Context context, String what) {
        Intent intent = new Intent(JUST_CHANGED);
        intent.putExtra(JUST_CHANGED, what);
        context.sendBroadcast(intent);
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
