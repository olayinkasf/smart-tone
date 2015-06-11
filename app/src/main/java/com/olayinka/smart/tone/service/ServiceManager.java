package com.olayinka.smart.tone.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Olayinka on 5/9/2015.
 */
public class ServiceManager {

    public static void stopAlarm(Context context, Class<? extends Service> service) {
        PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 0, new Intent(context.getApplicationContext(), service), 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public static void startAlarm(Context context, Class<? extends Service> service) {
        stopAlarm(context, service);
        PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 0, new Intent(context.getApplicationContext(), service), 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10 * 1000, pendingIntent);
    }

}
