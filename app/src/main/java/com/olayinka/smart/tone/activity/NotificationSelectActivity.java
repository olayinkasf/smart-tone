package com.olayinka.smart.tone.activity;

import android.os.Bundle;
import com.olayinka.smart.tone.AppSettings;
import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 5/8/2015.
 */
public class NotificationSelectActivity extends RingtoneSelectActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefsKey = AppSettings.ACTIVE_NOTIFICATION;
        mToastMessageId = R.string.notification_selected;
        mForceKey = AppSettings.FORCE_CHANGE_NOTIF;
    }
}
