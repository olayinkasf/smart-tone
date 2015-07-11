package com.olayinka.smart.tone.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.BuildConfig;
import com.olayinka.smart.tone.R;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.service.NotifAccessibilityService;
import com.olayinka.smart.tone.service.ServiceManager;
import com.olayinka.smart.tone.service.ShuffleService;

import java.util.Map;

/**
 * Created by Olayinka on 5/9/2015.
 */
public class MenuActivity extends AnotherMenuActivity {

    private AlertDialog mDialog;

    @Override
    protected void getVersion(Map<String, String> varMap) {
        varMap.put("version.code", String.valueOf(BuildConfig.VERSION_CODE));
        varMap.put("version.name", BuildConfig.VERSION_NAME);
        varMap.put("build.type", BuildConfig.BUILD_TYPE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (NotifAccessibilityService.shouldRun(this) && !NotifAccessibilityService.isEnabled(this)) {
            mDialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setPositiveButton(lib.olayinka.smart.tone.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Utils.removeNotification(MenuActivity.this);
                            if (NotifAccessibilityService.shouldRun(MenuActivity.this) && !NotifAccessibilityService.isEnabled(MenuActivity.this)) {
                                ServiceManager.stopAlarm(MenuActivity.this, NotifAccessibilityService.class);
                                //Start repeating change service
                                AppSettings.setFreq(MenuActivity.this, AppSettings.NOTIF_FREQ, 1, R.array.notification_freq);
                                ServiceManager.startAlarm(MenuActivity.this, ShuffleService.class);
                                Utils.toast(MenuActivity.this, R.string.notification_freq_changed);
                            }
                        }
                    })
                    .setMessage(R.string.requires_accessibility)
                    .show();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }
}
