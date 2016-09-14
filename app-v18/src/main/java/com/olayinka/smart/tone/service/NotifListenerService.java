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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;

import com.olayinka.smart.tone.AppLogger;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.PermissionUtils;
import com.olayinka.smart.tone.R;
import com.olayinka.smart.tone.Utils;

/**
 * Created by Olayinka on 5/9/2015.
 */
public class NotifListenerService extends NotificationListenerService {

    private static final String NOTIF_CANCELED = "notification.request.notif.canceled";

    private final Handler mHandler = new Handler();
    private final Runnable mPermissionRunnable = new Runnable() {
        @Override
        public void run() {
            if (PermissionUtils.hasNotificationPermission(NotifListenerService.this)) {
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.cancel(R.id.notifListenerNotif);
                mHandler.removeCallbacks(this);
            } else mHandler.postDelayed(this, 500);
        }
    };

    public static boolean shouldRun(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        return prefs.getLong(AppSettings.NOTIF_FREQ, 0) == 0
                && prefs.getLong(AppSettings.ACTIVE_NOTIFICATION, 0) != 0
                && prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals(getPackageName()))
            return;
        AppLogger.wtf(this, "onNotificationPosted", sbn.toString());
        if (shouldRun(this)) AppSettings.changeNotificationSound(this);
        else stopSelf();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null )
            handleIntent(intent);
        return START_STICKY;
    }

    public int handleIntent(Intent intent) {

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (!shouldRun(this)) {
            mNotifyMgr.cancel(R.id.notifListenerNotif);
            AppLogger.wtf(this, "onStartCommand/NotifListenerService", "Shouldn't run! Stop alarm and return START_NOT_STICKY");
            stopSelf();
            return START_NOT_STICKY;
        }

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        String action = intent.getAction();
        if (action!= null && action.equals(NOTIF_CANCELED)) {
            if (!PermissionUtils.hasNotificationPermission(this)) {
                AppSettings.setFreq(this, AppSettings.NOTIF_FREQ, 1, R.array.notification_freq);
                ShuffleStub.Notification.start(this);
                mNotifyMgr.notify(
                        R.id.notifListenerNotif,
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_notif_small)
                                .setLargeIcon(Utils.getLargeIcon(this))
                                .setContentTitle(getString(R.string.notification_service))
                                .setContentText(getString(R.string.change_notif_freq))
                                .setTicker(getString(R.string.change_notif_freq))
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.change_notif_freq)))
                                .setSound(uri)
                                .build()
                );
                stopSelf();
                return START_NOT_STICKY;
            }
            return START_STICKY;
        }

        if (!PermissionUtils.hasNotificationPermission(this)) {
            Intent positiveIntent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            Intent cancelIntent = new Intent(this, NotifListenerService.class);
            cancelIntent.setAction(NOTIF_CANCELED);
            PendingIntent positivePendingIntent = PendingIntent.getActivity(this, 0, positiveIntent, 0);
            PendingIntent cancelPendingIntent = PendingIntent.getService(getApplicationContext(), 0, cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            mNotifyMgr.notify(
                    R.id.notifListenerNotif,
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_notif_small)
                            .setLargeIcon(Utils.getLargeIcon(this))
                            .setContentTitle(getString(R.string.notification_service))
                            .setContentText(getString(R.string.grant_notification_listener))
                            .setTicker(getString(R.string.grant_notification_listener))
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.requires_notification_listener)))
                            .setContentIntent(positivePendingIntent)
                            .setSound(uri)
                            .addAction(R.drawable.ic_lock_open_black_24dp, getString(R.string.grant_now), positivePendingIntent)
                            .addAction(R.drawable.ic_clear_black_24dp, getString(R.string.cancel), cancelPendingIntent)
                            .setOngoing(true)
                            .build()
            );
            mHandler.postDelayed(mPermissionRunnable, 500);
            return START_STICKY;
        }

        mNotifyMgr.cancel(R.id.notifListenerNotif);

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mHandler.removeCallbacks(mPermissionRunnable);
        } catch (Throwable ignored) {
        }
    }
}
