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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.olayinka.smart.tone.AppSettings;
import org.json.JSONException;

/**
 * Created by Olayinka on 5/9/2015.
 */
public class RingtoneTelephonyService extends Service {


    private PhoneStateListener mListener;

    @Override
    public void onCreate() {
        Log.wtf("onCreate", "New telephony service instance." + this);
        super.onCreate();
        if (mListener == null) {
            Log.wtf("onCreate", "New telephony listener instance." + this);
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
        if (!shouldRun()) return;
        try {
            AppSettings.changeRingtoneSound(this);
        } catch (JSONException e) {
            Log.wtf("onNotificationPosted", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!shouldRun()) {
            Log.wtf("onStartCommand/RingtoneTelephonyService", "Shouldn't run! Stop alarm and return START_NOT_STICKY");
            ServiceManager.stopAlarm(getApplicationContext(), RingtoneTelephonyService.class);
            destroyListener();
            stopSelf();
            return START_NOT_STICKY;
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
            Log.wtf("onCreate", "Telephony listener instance destroyed." + this);
            mTelephonyMgr.listen(mListener, PhoneStateListener.LISTEN_NONE);
        }
        mListener = null;
    }

}
