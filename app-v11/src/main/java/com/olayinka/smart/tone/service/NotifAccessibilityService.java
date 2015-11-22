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
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.view.accessibility.AccessibilityEvent;
import com.olayinka.smart.tone.*;
import org.json.JSONException;

import java.util.List;

public class NotifAccessibilityService extends AccessibilityService {


    private long mLastNotifiedListener = 0;


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
        AppLogger.wtf(this, "onNotificationPosted", event.toString());
        final int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Parcelable parcelable = event.getParcelableData();
            if (parcelable instanceof Notification) {
                List<CharSequence> messages = event.getText();
                if (messages.size() > 0) {
                    try {
                        if (!shouldRun(this)) stopSelf();
                        else AppSettings.changeNotificationSound(this, false);

                    } catch (JSONException e) {
                        AppLogger.wtf(this, "onNotificationPosted", e);
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (!shouldRun(this)) {
            AppLogger.wtf(this, "onStartCommand/NotifAccessibilityService", "Shouldn't run! Stop alarm and return START_NOT_STICKY");
            ServiceManager.stopAlarm(getApplicationContext(), NotifAccessibilityService.class);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent.getBooleanExtra(AppSettings.NOTIF_CANCELED, false) && !PermissionUtils.hasNotificationPermission(this)) {
            ServiceManager.stopAlarm(this, NotifAccessibilityService.class);
            //Start repeating change service
            AppSettings.setFreq(this, AppSettings.NOTIF_FREQ, 1, R.array.notification_freq);
            ServiceManager.startAlarm(this, ShuffleService.class);
            stopSelf();
            return START_NOT_STICKY;
        }

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (!PermissionUtils.hasNotificationPermission(this)) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notif_small)
                    .setLargeIcon(Utils.getLargeIcon(this))
                    .setContentTitle(getString(R.string.notification_service))
                    .setContentText(getString(R.string.grant_accessibility))
                    .setTicker(getString(R.string.grant_accessibility))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.grant_accessibility)))
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 0));
            intent.putExtra(AppSettings.NOTIF_CANCELED, true);
            mBuilder.setDeleteIntent(PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
            if (System.currentTimeMillis() - mLastNotifiedListener > 15 * 60 * 1000) {
                mBuilder.setSound(uri);
                mLastNotifiedListener = System.currentTimeMillis();
            } else
                mBuilder.setSound(null).setVibrate(new long[]{0L});
            Notification notification = mBuilder.build();
            notification.flags = Notification.DEFAULT_LIGHTS;
            mNotifyMgr.notify(R.id.notifListenerNotif, notification);
        } else {
            mNotifyMgr.cancel(R.id.notifListenerNotif);
        }

        return START_NOT_STICKY;

    }
}