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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.model.Media;

import java.io.IOException;
import java.util.HashMap;

import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 11/4/2014.
 */
public class MediaPlayerStub extends SmartToneStub {

    private static final String ACTION_PLAY_SOUND = "action.play.sound";
    private static final String ACTION_STOP_SOUND = "action.stop.sound";
    private static final String EXTRA_IS_NOTIF = "extra.notify";
    private static final String EXTRA_CHANGE_PARAMS = "extra.change.params";
    private static final String ACTION_NOTIF_DELETED = "action.notif.deleted";
    private static final String ACTION_NOTIF_CREATED = "action.notif.created";
    private static final String EXTRA_MEDIA_OBJECT = "extra.media.object";

    public static final String ACTION_FILTER = "media.service";

    private MediaPlayer mMediaPlayer;
    private final AudioManager mAudioManager;
    private HashMap<Integer, Intent> mNotifs = new HashMap<>();

    public MediaPlayerStub(SmartToneService service, AudioManager audioManager) {
        super(service);
        this.mAudioManager = audioManager;
    }

    @Override
    public synchronized void handleIntent(Intent intent) {
        handleIntent(intent, 0);
    }

    @Override
    protected String actionFilter() {
        return ACTION_FILTER;
    }

    @Override
    public void selfDestroy() {
        stop(true);
    }

    private synchronized void handleIntent(Intent intent, int notifId) {
        AppSettings.ChangeParams changeParams = intent.getParcelableExtra(EXTRA_CHANGE_PARAMS);
        String action = intent.getAction().split(SmartToneService.ACTION_SEP)[2];
        Uri uri = intent.getData();
        ContentValues media = intent.getParcelableExtra(EXTRA_MEDIA_OBJECT);
        boolean isNotif = intent.getBooleanExtra(EXTRA_IS_NOTIF, false);
        if (isNotif && changeParams.getNotifId() == notifId)
            return;
        switch (action) {
            case ACTION_NOTIF_CREATED:
                mNotifs.put(changeParams.getNotifId(), intent);
                created(changeParams, uri, isNotif, media);
                break;
            case ACTION_NOTIF_DELETED:
                deleted(changeParams, uri, isNotif);
                break;
            case ACTION_PLAY_SOUND:
                play(changeParams, uri, isNotif, media);
                break;
            case ACTION_STOP_SOUND:
                stop(changeParams, uri, isNotif, media);
                break;
        }
    }

    private synchronized void created(AppSettings.ChangeParams changeParams, Uri uri, boolean isNotif, ContentValues media) {
        notify(changeParams, uri, ACTION_PLAY_SOUND, media);
        if (mMediaPlayer != null && mMediaPlayer.changeParams != null && mMediaPlayer.changeParams.getNotifId() == changeParams.getNotifId()) {
            stop(true);
        }
    }

    private synchronized void play(AppSettings.ChangeParams changeParams, Uri uri, boolean isNotif, ContentValues media) {
        stop(false);
        try {
            start(uri, changeParams);
            if (isNotif)
                notify(changeParams, uri, ACTION_STOP_SOUND, media);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private synchronized void stop(AppSettings.ChangeParams changeParams, Uri uri, boolean isNotif, ContentValues media) {
        if (isNotif) {
            stop(true);
            notify(changeParams, uri, ACTION_PLAY_SOUND, media);
        } else if (mMediaPlayer != null && mMediaPlayer.changeParams == null) {
            stop(true);
        }
    }

    private synchronized void deleted(AppSettings.ChangeParams changeParams, Uri uri, boolean isNotif) {
        mNotifs.remove(changeParams.getNotifId());
        if (mMediaPlayer != null && mMediaPlayer.uri.equals(uri))
            stop(true);
    }

    public synchronized void stop(boolean abandon) {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            if (abandon) {
                mAudioManager.abandonAudioFocus(null);
            }
        }
        mMediaPlayer = null;
        for (Intent intent : mNotifs.values()) {
            handleIntent(intent, 0);
        }
    }

    public synchronized void start(Uri uri, AppSettings.ChangeParams changeParams) throws IOException {

        int result = mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mService, uri, changeParams);
            mMediaPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(android.media.MediaPlayer mp) {
                    for (Intent intent : mNotifs.values()) {
                        handleIntent(intent, 0);
                    }
                }
            });
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        }
    }

    public static synchronized void stop(Context context) {
        context.startService(SmartToneService.action(context, ACTION_FILTER, ACTION_STOP_SOUND));
    }

    public static synchronized void start(Context context, Uri uri) {
        Intent intent = SmartToneService.action(context, ACTION_FILTER, ACTION_PLAY_SOUND);
        intent.setData(uri);
        context.startService(intent);
    }

    public static void notify(Context context, AppSettings.ChangeParams changeParams, ContentValues mediaContentValues)  {
        Intent intent = SmartToneService.action(context, ACTION_FILTER, ACTION_NOTIF_CREATED);
        intent.setClass(context, SmartToneService.class);
        intent.putExtra(MediaPlayerStub.EXTRA_CHANGE_PARAMS, changeParams);
        intent.putExtra(MediaPlayerStub.EXTRA_IS_NOTIF, true);
        intent.putExtra(MediaPlayerStub.EXTRA_MEDIA_OBJECT, mediaContentValues);
        intent.setData(AppSettings.getToneUri(mediaContentValues));
        context.startService(intent);
    }


    public synchronized void notify(AppSettings.ChangeParams changeParams, Uri uri, String action, ContentValues media) {
        SharedPreferences preferences = mService.getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE);
        NotificationManager mNotifyMgr = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
        if (preferences.getBoolean(AppSettings.NOTIFY_CHANGE, false)) {
            Intent intent = new Intent(Settings.ACTION_SOUND_SETTINGS);

            Intent actionIntent = action(action, changeParams.getIdPrefsName());
            actionIntent.setData(uri);
            actionIntent.putExtra(EXTRA_MEDIA_OBJECT, media);
            actionIntent.putExtra(EXTRA_CHANGE_PARAMS, changeParams);
            actionIntent.putExtra(EXTRA_IS_NOTIF, true);
            NotificationCompat.Builder builder = Utils.notifyManager(
                    mService,
                    mService.getString(changeParams.getNotifMessage()),
                    media.getAsString(Media.Columns.NAME) + "\n" + media.getAsString(Media.Columns.ARTIST_NAME) + "-" + media.getAsString(Media.Columns.ALBUM_NAME),
                    PendingIntent.getActivity(mService, 0, intent, 0)
            );
            builder.addAction(getIcon(action), mService.getString(getText(action)), PendingIntent.getService(mService, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            Intent deleteIntent = new Intent(action(ACTION_NOTIF_DELETED, changeParams.getIdPrefsName()));
            deleteIntent.setData(uri);
            deleteIntent.putExtra(EXTRA_CHANGE_PARAMS, changeParams);
            deleteIntent.putExtra(EXTRA_MEDIA_OBJECT, media);
            deleteIntent.putExtra(EXTRA_IS_NOTIF, true);
            builder.setDeleteIntent(PendingIntent.getService(mService, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            Notification notification = builder.build();
            notification.flags = Notification.DEFAULT_LIGHTS;

            mNotifyMgr.notify(changeParams.getNotifId(), notification);
            return;
        }
        mNotifyMgr.cancel(R.id.changeRingtoneNotif);
        mNotifyMgr.cancel(R.id.changeNotifNotif);
    }

    private synchronized int getText(String action) {
        switch (action) {
            case ACTION_PLAY_SOUND:
                return R.string.preview;
            case ACTION_STOP_SOUND:
                return R.string.stop_preview;
        }
        throw new IllegalArgumentException(String.format("Action %s is not valid", action));
    }

    private synchronized int getIcon(String action) {
        switch (action) {
            case ACTION_PLAY_SOUND:
                return R.drawable.ic_play_circle_outline_black_24dp;
            case ACTION_STOP_SOUND:
                return R.drawable.ic_stop_black_24dp;
        }
        throw new IllegalArgumentException(String.format("Action %s is not valid", action));
    }

    private static class MediaPlayer extends android.media.MediaPlayer {

        private Uri uri;
        private AppSettings.ChangeParams changeParams;

        public void setDataSource(Context context, Uri uri, AppSettings.ChangeParams changeParams) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
            super.setDataSource(context, uri);
            this.uri = uri;
            this.changeParams = changeParams;
        }

    }
}