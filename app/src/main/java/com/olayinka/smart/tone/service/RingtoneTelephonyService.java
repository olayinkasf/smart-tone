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

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.olayinka.smart.tone.*;
import com.olayinka.smart.tone.activity.PermissionsActivity;
import lib.olayinka.smart.tone.R;
import org.json.JSONException;

/**
 * Created by Olayinka on 5/9/2015.
 */
public class RingtoneTelephonyService extends Service {


    private PhoneStateListener mListener;
    private long mLastNotifiedListener = 0;

    @Override
    public void onCreate() {
        AppLogger.wtf(this, "onCreate", "New telephony service instance." + this);
        super.onCreate();
        if (mListener == null) {
            AppLogger.wtf(this, "onCreate", "New telephony listener instance." + this);
            TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            mListener = new PhoneStateListener() {
                public boolean lookOut;

                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    switch (state) {
                        case TelephonyManager.CALL_STATE_IDLE:
                            if (lookOut)
                                onCallStateIdle();
                            lookOut = false;
                            break;
                        case TelephonyManager.CALL_STATE_RINGING:
                            lookOut = true;
                            return;
                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            };
            mTelephonyMgr.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    public void onCallStateIdle() {
        if (!shouldRun()) {
            destroyListener();
            stopSelf();
        }
        try {
            AppSettings.changeRingtone(this);
        } catch (JSONException e) {
            AppLogger.wtf(this, "onNotificationPosted", e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!shouldRun()) {
            AppLogger.wtf(this, "onStartCommand/RingtoneTelephonyService", "Shouldn't run! Stop alarm and return START_NOT_STICKY");
            ServiceManager.stopAlarm(getApplicationContext(), RingtoneTelephonyService.class);
            destroyListener();
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent.getBooleanExtra(AppSettings.NOTIF_CANCELED, false) && !PermissionUtils.hasSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            ServiceManager.stopAlarm(this, RingtoneTelephonyService.class);
            //Start repeating change service
            AppSettings.setFreq(this, AppSettings.RINGTONE_FREQ, 1, R.array.ringtone_freq);
            ServiceManager.startAlarm(this, ShuffleService.class);
            stopSelf();
            return START_NOT_STICKY;
        }


        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (!PermissionUtils.hasSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            Intent realIntent = new Intent();
            realIntent.setClass(this, PermissionsActivity.class);
            realIntent.putExtra(PermissionsActivity.REQUESTED_PERMISSIONS, PermissionUtils.PERMISSION_PHONE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notif_small)
                    .setLargeIcon(Utils.getLargeIcon(this))
                    .setContentTitle(getString(R.string.ringtone_service))
                    .setContentText(getString(R.string.grant_read_phone_state))
                    .setTicker(getString(R.string.grant_read_phone_state))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.grant_read_phone_state)))
                    .setContentIntent(PendingIntent.getActivity(this, 0, realIntent, 0))
                    .setSound(uri)
                    .setDeleteIntent(PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
            Notification notification = mBuilder.build();
            notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
            mNotifyMgr.notify(R.id.phoneStateNotif, notification);
        } else {
            mNotifyMgr.cancel(R.id.phoneStateNotif);
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean shouldRun() {
        SharedPreferences prefs = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        return prefs.getLong(AppSettings.RINGTONE_FREQ, 0) == 0
                && prefs.getLong(AppSettings.ACTIVE_RINGTONE, 0) != 0
                && prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyListener();
    }

    private void destroyListener() {
        TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (mListener != null) {
            AppLogger.wtf(this, "onCreate", "Telephony listener instance destroyed." + this);
            mTelephonyMgr.listen(mListener, PhoneStateListener.LISTEN_NONE);
        }
        mListener = null;
    }

}
