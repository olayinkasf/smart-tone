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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import com.olayinka.smart.tone.AppSettings;

/**
 * Created by Olayinka on 5/10/2015.
 */
public abstract class ShuffleStub extends SmartToneStub {

    public static final String ACTION_NEXT_SCHEDULE = "action.next.schedule";
    public static final String FORCE_CHANGE = "force.change";
    private final String mPrefsFreq;
    private final String mPrefsId;

    public ShuffleStub(SmartToneService service, String mPrefsFreq, String mPrefsId) {
        super(service);
        this.mPrefsFreq = mPrefsFreq;
        this.mPrefsId = mPrefsId;
    }

    private boolean shouldChange() {
        long changeFreq = mService.getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE).getLong(mPrefsFreq, 0L);
        long lastChange = mService.getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE).getLong(mPrefsFreq + AppSettings.LAST_CHANGE, 0L);
        return Math.abs(SystemClock.elapsedRealtime() - lastChange) >= changeFreq;
    }

    private long changeFreq() {
        return mService.getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE).getLong(mPrefsFreq, 0L);
    }

    public boolean shouldRun() {
        SharedPreferences prefs = mService.getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE);
        return prefs.getLong(mPrefsFreq, 0) != 0
                && prefs.getLong(mPrefsId, 0) != 0
                && prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false);
    }

    @Override
    void handleIntent(Intent intent) {
        if (shouldRun() && shouldChange()) {
            change();
            setAlarm();
        }
    }

    @Override
    public void selfDestroy() {
        Intent intent = action(ACTION_NEXT_SCHEDULE);
        PendingIntent pendingIntent = PendingIntent.getService(mService.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mService.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    protected abstract void change();

    public void setAlarm() {
        Intent intent = action(ACTION_NEXT_SCHEDULE);
        PendingIntent pendingIntent = PendingIntent.getService(mService.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mService.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + changeFreq(), pendingIntent);
    }

    public static class Ringtone extends ShuffleStub {
        public static final String ACTION_FILTER = "ringtone.shuffle.service";

        public Ringtone(SmartToneService service) {
            super(service, AppSettings.RINGTONE_FREQ, AppSettings.ACTIVE_RINGTONE);
        }

        @Override
        String actionFilter() {
            return ACTION_FILTER;
        }

        public static void start(Context context) {
            context.startService(SmartToneService.action(context, ACTION_FILTER, ACTION_NEXT_SCHEDULE));
        }

        @Override
        protected void change() {
            AppSettings.changeRingtone(mService);
        }
    }

    public static class Notification extends ShuffleStub {
        public static final String ACTION_FILTER = "notification.shuffle.service";

        public Notification(SmartToneService service) {
            super(service, AppSettings.NOTIF_FREQ, AppSettings.ACTIVE_NOTIFICATION);
        }

        @Override
        String actionFilter() {
            return ACTION_FILTER;
        }

        public static void start(Context context) {
            context.startService(SmartToneService.action(context, ACTION_FILTER, ACTION_NEXT_SCHEDULE));
        }

        @Override
        protected void change() {
            AppSettings.changeNotificationSound(mService);
        }
    }


}
