package com.olayinka.smart.tone.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import com.olayinka.smart.tone.AppSettings;
import org.json.JSONException;

/**
 * Created by Olayinka on 5/10/2015.
 */
public class ShuffleService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean runForRingtone = shouldRunForRingtone();
        boolean runForNotification = shouldRunForNotification();
        if (!runForNotification && !runForRingtone) {
            Log.wtf("onStartCommand/ShuffleService", "Shouldn't run! Stop alarm and return START_NOT_STICKY");
            ServiceManager.stopAlarm(getApplicationContext(), ShuffleService.class);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (runForRingtone &&
                (shouldChange(AppSettings.RINGTONE_FREQ) ||
                        (intent != null && intent.getBooleanExtra(AppSettings.FORCE_CHANGE_RINGTONE, false)))
                ) try {
            AppSettings.changeRingtoneSound(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (runForNotification &&
                (shouldChange(AppSettings.NOTIF_FREQ) ||
                        (intent != null && intent.getBooleanExtra(AppSettings.FORCE_CHANGE_NOTIF, false)))
                ) try {
            AppSettings.changeNotificationSound(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    private boolean shouldChange(String freq) {
        long changeFreq = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).getLong(freq, 0l);
        long lastChange = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).getLong(freq + AppSettings.LAST_CHANGE, 0l);
        return changeFreq != 0 && System.currentTimeMillis() - lastChange >= changeFreq;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean shouldRunForRingtone() {
        SharedPreferences prefs = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        return prefs.getLong(AppSettings.RINGTONE_FREQ, 0) != 0
                && prefs.getLong(AppSettings.ACTIVE_RINGTONE, 0) != 0
                && prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false);
    }

    public boolean shouldRunForNotification() {
        SharedPreferences prefs = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        return prefs.getLong(AppSettings.NOTIF_FREQ, 0) != 0
                && prefs.getLong(AppSettings.ACTIVE_NOTIFICATION, 0) != 0
                && prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false);
    }
}
