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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import com.olayinka.smart.tone.*;

/**
 * Created by Olayinka on 5/9/2015.
 */
public class NotifListenerService extends NotificationListenerService {

    public static boolean shouldRun(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        return prefs.getLong(AppSettings.NOTIF_FREQ, 0) == 0
                && prefs.getLong(AppSettings.ACTIVE_NOTIFICATION, 0) != 0
                && prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        AppLogger.wtf(this, "onNotificationPosted", sbn.toString());
        if (sbn.getNotification().contentView == null)
            return;
        try {
            if (shouldRun(this)) {
                AppSettings.changeNotificationSound(this, false);
            } else stopSelf();
        } catch (Throwable throwable) {
            AppLogger.wtf(this, "onNotificationPosted", throwable);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        AppLogger.wtf(this, "onNotificationRemoved", sbn.toString());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (!shouldRun(this)) {
            AppLogger.wtf(this, "onStartCommand/NotifListenerService", "Shouldn't run! Stop alarm and return START_NOT_STICKY");
            ServiceManager.stopAlarm(getApplicationContext(), NotifListenerService.class);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent.getBooleanExtra(AppSettings.NOTIF_CANCELED, false) && !PermissionUtils.hasNotificationPermission(this)) {
            ServiceManager.stopAlarm(this, NotifListenerService.class);
            //Start repeating change service
            AppSettings.setFreq(this, AppSettings.NOTIF_FREQ, 1, R.array.notification_freq);
            ServiceManager.startAlarm(this, ShuffleService.class);
            stopSelf();
            return START_NOT_STICKY;
        }

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (!PermissionUtils.hasNotificationPermission(this)) {
            intent = new Intent(this, NotifListenerService.class);
            intent.putExtra(AppSettings.NOTIF_CANCELED, true);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notif_small)
                    .setLargeIcon(Utils.getLargeIcon(this))
                    .setContentTitle(getString(R.string.notification_service))
                    .setContentText(getString(R.string.grant_notification_listener))
                    .setTicker(getString(R.string.grant_notification_listener))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.grant_notification_listener)))
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), 0))
                    .setSound(uri)
                    .setDeleteIntent(PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
            Notification notification = mBuilder.build();
            notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
            mNotifyMgr.notify(R.id.notifListenerNotif, notification);
        } else {
            mNotifyMgr.cancel(R.id.notifListenerNotif);
        }

        return START_NOT_STICKY;
    }
}
