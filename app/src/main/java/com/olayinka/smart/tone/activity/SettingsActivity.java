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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.olayinka.smart.tone.*;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.service.AppService;
import com.olayinka.smart.tone.service.IndexerService;
import com.olayinka.smart.tone.widget.PrefsSwitchCompat;
import com.olayinka.smart.tone.widget.PrefsTextView;
import lib.olayinka.smart.tone.R;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    View.OnClickListener mFreqClickListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            final int arrayId = (int) v.getTag(R.id.array);
            final String key = ((PrefsTextView) v.findViewById(R.id.text)).getPrefsValueKey();
            new AlertDialog.Builder(SettingsActivity.this).setItems(getResources().getStringArray(arrayId), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AppSettings.setFreq(SettingsActivity.this, key, which, arrayId);
                    PrefsTextView local = (PrefsTextView) v.findViewById(R.id.text);
                    local.setPrefsText();
                    ((AbsSmartTone) getApplication()).startServices();
                }
            }).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        setActionBar();

        //fix unset text for shuffle frequency
        fixPrefsText(AppSettings.NOTIF_FREQ, R.array.notification_freq);
        fixPrefsText(AppSettings.RINGTONE_FREQ, R.array.ringtone_freq);

        //set app service prefs
        View view = findViewById(R.id.service);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(R.string.smarttone_service);
        PrefsSwitchCompat switchCompat = (PrefsSwitchCompat) view.findViewById(R.id.switchCompat);
        SharedPreferences prefs = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        boolean enabled = (prefs.getLong(AppSettings.ACTIVE_NOTIFICATION, 0) != 0
                || prefs.getLong(AppSettings.ACTIVE_RINGTONE, 0) != 0)
                && AppSqlHelper.hasData(this, Media.Collection.TABLE);
        switchCompat.setChecked(enabled & prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false));
        view.setEnabled(enabled);
        titleView.setEnabled(enabled);
        switchCompat.setEnabled(enabled);
        switchCompat.setOnCheckedChangeListener(this);
        view.setOnClickListener(this);


        //ringtone frequency
        view = findViewById(R.id.ringtoneFreq);
        view.setTag(R.id.array, R.array.ringtone_freq);
        titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(R.string.ringtone_freq);
        PrefsTextView textView = (PrefsTextView) view.findViewById(R.id.text);
        textView.setPrefs(AppSettings.APP_SETTINGS, AppSettings.RINGTONE_FREQ);
        textView.setPrefsText();
        view.setOnClickListener(mFreqClickListener);


        //notification frequency
        view = findViewById(R.id.notifFreq);
        view.setTag(R.id.array, R.array.notification_freq);
        titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(R.string.notification_freq);
        textView = (PrefsTextView) view.findViewById(R.id.text);
        textView.setPrefs(AppSettings.APP_SETTINGS, AppSettings.NOTIF_FREQ);
        textView.setPrefsText();
        view.setOnClickListener(mFreqClickListener);

        //notify on ringtone change only
        view = findViewById(R.id.shuffleMode);
        switchCompat = (PrefsSwitchCompat) view.findViewById(R.id.switchCompat);
        switchCompat.setPrefs(AppSettings.APP_SETTINGS, AppSettings.ORDER_CHANGE);
        titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(R.string.order_change);
        view.setOnClickListener(this);


        //notify on ringtone change only
        view = findViewById(R.id.notifyChange);
        switchCompat = (PrefsSwitchCompat) view.findViewById(R.id.switchCompat);
        switchCompat.setPrefs(AppSettings.APP_SETTINGS, AppSettings.NOTIFY_CHANGE);
        titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(R.string.notify_change);
        view.setOnClickListener(this);

        //Keep system log
        view = findViewById(R.id.sendLog);
        switchCompat = (PrefsSwitchCompat) view.findViewById(R.id.switchCompat);
        switchCompat.setPrefs(AppSettings.APP_SETTINGS, AppSettings.LOG_APP_ACTIVITY);
        titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(R.string.log_app_activity);
        view.setOnClickListener(this);


        //backup collections
        view = findViewById(R.id.backup);
        titleView = (TextView) view.findViewById(R.id.title);
        view.findViewById(R.id.text).setVisibility(View.GONE);
        titleView.setText(R.string.backup_collections);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(v.getContext(), AppService.class);
                intent.setType(AppService.EXPORT_COLLECTIONS);
                Utils.toast(v.getContext(), R.string.collection_export_sent);
                v.getContext().startService(intent);
            }
        });

        //rescan media files
        view = findViewById(R.id.reindex);
        titleView = (TextView) view.findViewById(R.id.title);
        view.findViewById(R.id.text).setVisibility(View.GONE);
        titleView.setText(R.string.rescan_media);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new AlertDialog.Builder(v.getContext())
                        .setMessage(R.string.reindex_warning)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(v.getContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(IndexerService.FORCE_INDEX, true);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });


        //about app
        view = findViewById(R.id.aboutApp);
        titleView = (TextView) view.findViewById(R.id.title);
        textView = (PrefsTextView) view.findViewById(R.id.text);
        textView.setText(String.format("SmartTone v%s", Utils.VAR_MAP.get("version.name")));
        titleView.setText(R.string.about_app);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                v.getContext().startActivity(new Intent(v.getContext(), AboutAppActivity.class));
            }
        });

    }

    private void setActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.settings);

    }

    void fixPrefsText(String key, int arrayId) {
        if (getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).getString(key + AppSettings.TEXT, null) == null) {
            getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE)
                    .edit().putString(key + AppSettings.TEXT, getResources().getStringArray(arrayId)[0])
                    .apply();
        }
    }

    @Override
    public void onClick(View v) {
        PrefsSwitchCompat switchCompat = (PrefsSwitchCompat) v.findViewById(R.id.switchCompat);
        switchCompat.setChecked(!switchCompat.isChecked());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ((AbsSmartTone) getApplication()).startServices();
    }
}
