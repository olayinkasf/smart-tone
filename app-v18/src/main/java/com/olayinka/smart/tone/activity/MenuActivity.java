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

package com.olayinka.smart.tone.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.BuildConfig;
import com.olayinka.smart.tone.R;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.service.NotifListenerService;
import com.olayinka.smart.tone.service.ServiceManager;
import com.olayinka.smart.tone.service.ShuffleService;

import java.util.Map;

/**
 * Created by Olayinka on 5/9/2015.
 */
public class MenuActivity extends AnotherMenuActivity {

    private AlertDialog mDialog;

    @Override
    protected void onStart() {
        super.onStart();
        if (NotifListenerService.shouldRun(this) && !NotifListenerService.isEnabled(this)) {
            mDialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setPositiveButton(lib.olayinka.smart.tone.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Utils.removeNotification(MenuActivity.this);
                            if (NotifListenerService.shouldRun(MenuActivity.this) && !NotifListenerService.isEnabled(MenuActivity.this)) {
                                ServiceManager.stopAlarm(MenuActivity.this, NotifListenerService.class);
                                //Start repeating change service
                                AppSettings.setFreq(MenuActivity.this, AppSettings.NOTIF_FREQ, 1, R.array.notification_freq);
                                ServiceManager.startAlarm(MenuActivity.this, ShuffleService.class);
                                Utils.toast(MenuActivity.this, R.string.notification_freq_changed);
                            }
                        }
                    })
                    .setMessage(R.string.requires_notification_listener)
                    .show();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected void getVersion(Map<String, String> varMap) {
        varMap.put("version.code", String.valueOf(BuildConfig.VERSION_CODE));
        varMap.put("version.name", BuildConfig.VERSION_NAME);
        varMap.put("build.type", BuildConfig.BUILD_TYPE);
    }

}
