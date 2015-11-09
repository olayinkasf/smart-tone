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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import com.olayinka.smart.tone.AppLogger;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.R;
import com.olayinka.smart.tone.Utils;
import org.json.JSONException;

/**
 * Created by Olayinka on 5/9/2015.
 */
public class NotifListenerService extends NotificationListenerService {

    public static boolean isEnabled(Context context) {
        ComponentName cn = new ComponentName(context, NotifListenerService.class);
        String flat = android.provider.Settings.Secure.getString(context.getContentResolver(), AppSettings.ENABLED_NOTIFICATION_LISTENERS);
        return flat != null && flat.contains(cn.flattenToString());
    }

    public static boolean shouldRun(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        return prefs.getLong(AppSettings.NOTIF_FREQ, 0) == 0
                && prefs.getLong(AppSettings.ACTIVE_NOTIFICATION, 0) != 0
                && prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            AppLogger.wtf(this, "onNotificationPosted", sbn.toString());
            if (shouldRun(this)) AppSettings.changeNotificationSound(this, false);
        } catch (JSONException e) {
            AppLogger.wtf(this, "onNotificationPosted", e);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        AppLogger.wtf(this, "onNotificationRemoved", sbn.toString());
    }

    Bitmap mLargeIcon;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!shouldRun(this)) {
            AppLogger.wtf(this, "onStartCommand/NotifListenerService", "Shouldn't run! Stop alarm and return START_NOT_STICKY");
            ServiceManager.stopAlarm(getApplicationContext(), NotifListenerService.class);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (mLargeIcon == null) {
            mLargeIcon = BitmapFactory.decodeResource(getResources(), lib.olayinka.smart.tone.R.mipmap.ic_notif_large);
        }
        if (!isEnabled(this)) {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notif_small)
                    .setLargeIcon(mLargeIcon)
                    .setContentTitle(getString(R.string.notification_service))
                    .setContentText(getString(R.string.grant_notification_listener))
                    .setTicker(getString(R.string.grant_notification_listener))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.grant_notification_listener)))
                    .setSound(uri)
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), 0));
            int mNotificationId = R.id.appNotifSettings;
            Notification notification = mBuilder.build();
            notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL | Notification.FLAG_NO_CLEAR;
            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(mNotificationId, notification);
        } else {
            Utils.removeNotification(this);
        }
        return START_NOT_STICKY;
    }
}
