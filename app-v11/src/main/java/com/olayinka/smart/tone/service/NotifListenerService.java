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

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.view.accessibility.AccessibilityEvent;

import com.olayinka.smart.tone.AppLogger;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.PermissionUtils;
import com.olayinka.smart.tone.R;
import com.olayinka.smart.tone.Utils;

public class NotifListenerService extends AccessibilityService {

    private static final String NOTIF_CANCELED = "accessibility.request.notif.canceled";

    private final Handler mHandler = new Handler();
    private final Runnable mPermissionRunnable = new Runnable() {
        @Override
        public void run() {
            if (PermissionUtils.hasAccessibilityPermission(NotifListenerService.this)) {
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
    public void onServiceConnected() {
        AppLogger.wtf(this, "onServiceConnected", "service connected successfully");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_AUDIBLE | AccessibilityServiceInfo.FEEDBACK_VISUAL;
        info.notificationTimeout = 100;

        this.setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName().equals(getPackageName()))
            return;
        AppLogger.wtf(this, "onNotificationPosted", event.toString());
        final int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Parcelable parcelable = event.getParcelableData();
            if (parcelable instanceof Notification) {
                Notification notification = (Notification) parcelable;
                if (notification.when + 1000 >= System.currentTimeMillis()) {
                    if (!shouldRun(this)) stopSelf();
                    else AppSettings.changeNotificationSound(this);
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

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
            AppLogger.wtf(this, "onStartCommand/NotifListenerService", "Shouldn't run! Stop alarm and return START_NOT_STICKY");
            mNotifyMgr.cancel(R.id.notifListenerNotif);
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

        if (!PermissionUtils.hasAccessibilityPermission(this)) {
            Intent positiveIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            Intent cancelIntent = new Intent(this, NotifListenerService.class);
            cancelIntent.setAction(NOTIF_CANCELED);

            PendingIntent positivePendingIntent = PendingIntent.getActivity(this, 0, positiveIntent, 0);
            PendingIntent cancelPendingIntent = PendingIntent.getService(getApplicationContext(), 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotifyMgr.notify(
                    R.id.notifListenerNotif,
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_notif_small)
                            .setLargeIcon(Utils.getLargeIcon(this))
                            .setContentTitle(getString(R.string.notification_service))
                            .setContentText(getString(R.string.grant_accessibility))
                            .setTicker(getString(R.string.grant_accessibility))
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.requires_accessibility)))
                            .setContentIntent(positivePendingIntent)
                            .setSound(uri)
                            .addAction(R.drawable.ic_lock_open_black_24dp, getString(R.string.grant_now), positivePendingIntent)
                            .addAction(R.drawable.ic_clear_black_24dp, getString(R.string.cancel), cancelPendingIntent)
                            .setOngoing(true).build()
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