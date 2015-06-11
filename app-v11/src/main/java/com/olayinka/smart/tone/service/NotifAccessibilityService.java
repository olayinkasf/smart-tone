package com.olayinka.smart.tone.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.R;
import com.olayinka.smart.tone.Utils;
import org.json.JSONException;

import java.util.List;

public class NotifAccessibilityService extends AccessibilityService {


    private Bitmap mLargeIcon;

    public static boolean isEnabled(Context context) {
        ComponentName cn = new ComponentName(context, NotifAccessibilityService.class);
        String flat = android.provider.Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return flat != null && flat.contains(cn.flattenToString());
    }

    public static boolean shouldRun(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        return prefs.getLong(AppSettings.NOTIF_FREQ, 0) == 0
                && prefs.getLong(AppSettings.ACTIVE_NOTIFICATION, 0) != 0
                && prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false);
    }

    @Override
    public void onServiceConnected() {
        Log.wtf("onServiceConnected", "service connected successfully");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_AUDIBLE | AccessibilityServiceInfo.FEEDBACK_VISUAL;
        info.notificationTimeout = 100;

        this.setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.wtf("onNotificationPosted", event.toString());
        final int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Parcelable parcelable = event.getParcelableData();
            if (parcelable instanceof Notification) {
                List<CharSequence> messages = event.getText();
                if (messages.size() > 0) {
                    try {
                        if (shouldRun(this)) AppSettings.changeNotificationSound(this, false);
                    } catch (JSONException e) {
                        Log.wtf("onNotificationPosted", e);
                    }
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!shouldRun(this)) {
            Log.wtf("onStartCommand/NotifAccessibilityService", "Shouldn't run! Stop alarm and return START_NOT_STICKY");
            ServiceManager.stopAlarm(getApplicationContext(), NotifAccessibilityService.class);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (mLargeIcon == null) {
            mLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.large_notif_icon);
        }
        if (!isEnabled(this)) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.small_notif_icon)
                    .setLargeIcon(mLargeIcon)
                    .setContentTitle(getString(R.string.notification_service))
                    .setContentText(getString(R.string.grant_accessibility))
                    .setTicker(getString(R.string.grant_accessibility))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.grant_accessibility)))
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 0));
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

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}