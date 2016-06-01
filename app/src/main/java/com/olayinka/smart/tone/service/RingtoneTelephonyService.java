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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.olayinka.smart.tone.AppLogger;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.PermissionUtils;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.activity.PermissionsActivity;

import org.json.JSONException;

import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 5/9/2015.
 */
public class RingtoneTelephonyService extends Service {


    private PhoneStateListener mListener;
    private final Handler mHandler = new Handler();
    private final Runnable mPermissionRunnable = new Runnable() {
        @Override
        public void run() {
            if (PermissionUtils.hasSelfPermission(RingtoneTelephonyService.this, Manifest.permission.READ_PHONE_STATE)) {
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.cancel(R.id.phoneStateNotif);
                mHandler.removeCallbacks(this);
            }else mHandler.postDelayed(this, 500);
        }
    };

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
            return;
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

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (intent.getBooleanExtra(AppSettings.NOTIF_CANCELED, false) && !PermissionUtils.hasSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            ServiceManager.stopAlarm(this, RingtoneTelephonyService.class);
            //Start repeating change service
            AppSettings.setFreq(this, AppSettings.RINGTONE_FREQ, 1, R.array.ringtone_freq);
            ServiceManager.startAlarm(this, ShuffleService.class);
            stopSelf();
            mNotifyMgr.notify(
                    R.id.phoneStateNotif,
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_notif_small)
                            .setLargeIcon(Utils.getLargeIcon(this))
                            .setContentTitle(getString(R.string.notification_service))
                            .setContentText(getString(R.string.change_ringtone_freq))
                            .setTicker(getString(R.string.change_ringtone_freq))
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.change_ringtone_freq)))
                            .setSound(uri)
                            .build()
            );
            return START_NOT_STICKY;
        }

        if (!PermissionUtils.hasSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            Intent positiveIntent = new Intent(this, PermissionsActivity.class);
            Intent negativeIntent = new Intent(this, RingtoneTelephonyService.class);
            negativeIntent.putExtra(AppSettings.NOTIF_CANCELED, true);
            positiveIntent.putExtra(PermissionsActivity.REQUESTED_PERMISSIONS, PermissionUtils.PERMISSION_PHONE);
            PendingIntent positivePendingIntent = PendingIntent.getActivity(this, 0, positiveIntent, 0);
            PendingIntent cancelPendingIntent = PendingIntent.getService(getApplicationContext(), 0, negativeIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            mNotifyMgr.notify(
                    R.id.phoneStateNotif,
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_notif_small)
                            .setLargeIcon(Utils.getLargeIcon(this))
                            .setContentTitle(getString(R.string.ringtone_service))
                            .setContentText(getString(R.string.grant_read_phone_state))
                            .setTicker(getString(R.string.grant_read_phone_state))
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.grant_read_phone_state)))
                            .setContentIntent(positivePendingIntent)
                            .setSound(uri)
                            .addAction(R.drawable.ic_lock_open_black_24dp, getString(R.string.grant_now), positivePendingIntent)
                            .addAction(R.drawable.ic_clear_black_24dp, getString(R.string.cancel), cancelPendingIntent)
                            .setOngoing(true).build()
            );
            mHandler.postDelayed(mPermissionRunnable, 500);
            return START_NOT_STICKY;
        }

        mNotifyMgr.cancel(R.id.phoneStateNotif);

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
