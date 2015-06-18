package com.olayinka.smart.tone.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import com.olayinka.smart.tone.AbsSmartTone;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.service.ShuffleService;
import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 5/8/2015.
 */
public class RingtoneSelectActivity extends CollectionPickerActivity {

    protected String mPrefsKey;
    protected int mToastMessageId;
    protected String mForceKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefsKey = AppSettings.ACTIVE_RINGTONE;
        mToastMessageId = R.string.ringtone_selected;
        mForceKey = AppSettings.FORCE_CHANGE_RINGTONE;
    }

    private void picked(long id) {
        getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).edit().putLong(mPrefsKey, id).apply();
        Intent intent = new Intent(this, ShuffleService.class);
        intent.putExtra(mForceKey, true);
        startService(intent);
        Utils.toast(this, mToastMessageId);
        ((AbsSmartTone) getApplication()).startServices();
        finish();
    }

    @Override
    public void onClick(View v) {
        final long id = (long) v.getTag(R.id.collectionId);
        if (!getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).getBoolean(AppSettings.ACTIVE_APP_SERVICE, false)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.activate_service)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).edit().putBoolean(AppSettings.ACTIVE_APP_SERVICE, true).apply();
                            picked(id);
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            picked(id);
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            picked(id);
        }
    }
}
