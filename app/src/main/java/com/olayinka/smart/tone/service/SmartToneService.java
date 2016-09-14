package com.olayinka.smart.tone.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.SystemClock;

import java.util.HashMap;

public class SmartToneService extends Service {

    HashMap<String, SmartToneStub> mStubs = new HashMap<>(10);
    public static final String ACTION = "com.olayinka.smart.tone";
    public static final String ACTION_SEP = "/";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mStubs.put(
                MediaPlayerStub.ACTION_FILTER,
                new MediaPlayerStub(this, (AudioManager) getSystemService(AUDIO_SERVICE))
        );
        mStubs.put(
                ShuffleStub.Ringtone.ACTION_FILTER,
                new ShuffleStub.Ringtone(this)
        );
        mStubs.put(
                ShuffleStub.Notification.ACTION_FILTER,
                new ShuffleStub.Notification(this)
        );

        startServices(this);
    }

    public Intent action(String filter, String... actions) {
        return action(this, filter, actions);
    }

    public static Intent action(Context context, String filter, String... actions) {
        StringBuilder builder = new StringBuilder();
        builder.append(ACTION).append(ACTION_SEP).append(filter);
        for (String action : actions)
            builder.append(ACTION_SEP).append(action);
        Intent intent = new Intent(context, SmartToneService.class);
        intent.setAction(builder.toString());
        return intent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null)
            handleIntent(intent);
        return START_STICKY;
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        String[] actions = action.split(ACTION_SEP);
        if (actions.length > 1)
            mStubs.get(intent.getAction().split(ACTION_SEP)[1]).handleIntent(intent);
    }


    /**
     * start all services regardless of previous state
     * because app was killed and we must check for everything
     */
    public static void startServices(Context context) {
        context.startService(new Intent(context, SmartToneService.class));
        context.startService(new Intent(context, RingtoneTelephonyService.class));
        ShuffleStub.Ringtone.start(context);
        ShuffleStub.Notification.start(context);
        startNotifListener(context);
    }

    private static void startNotifListener(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context.getPackageName(), "com.olayinka.smart.tone.service.NotifListenerService"));
        context.startService(intent);
    }

    /**
     * start services because the service was switched back on
     */
    public static void startServicesCheckChanged(Context context) {
        startServices(context);
    }

    /**
     * start only ringtone specific services because ringtone params was modified
     */
    public static void startServicesRingtoneChanged(Context context) {
        context.startService(new Intent(context, RingtoneTelephonyService.class));
        ShuffleStub.Ringtone.start(context);
    }

    /**
     * start only notification specific services because notification params was modified
     */
    public static void startServicesNotifChanged(Context context) {
        ShuffleStub.Notification.start(context);
        startNotifListener(context);
    }

    @Override
    public void onDestroy() {
        for (SmartToneStub stub : mStubs.values()) {
            stub.selfDestroy();
        }
        timeTravel();
        super.onDestroy();
    }

    private void timeTravel() {
        Intent intent = new Intent(this, SmartToneService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 3000, pendingIntent);
    }
}
