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

package com.olayinka.smart.tone;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import com.olayinka.smart.tone.activity.AbstractMenuActivity;
import com.olayinka.smart.tone.activity.PermissionsActivity;
import com.olayinka.smart.tone.service.IndexerService;
import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 4/19/2015.
 */
public class MainActivity extends Activity {
    boolean mReceiverUnregistered = true;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppLogger.wtf(context, "onStart", "mReceiver");
                unregisterReceiver(this);
                mReceiverUnregistered = true;
                intent = new Intent();
                intent.setComponent(new ComponentName(MainActivity.this, AbstractMenuActivity.CONCRETE));
                startActivity(intent);
                finish();
            }
        };
        if (PermissionUtils.hasSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            startService();
        else {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.permission_alert)
                    .setPositiveButton(R.string.done, null)
                    .show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, PermissionsActivity.class);
                    intent.putExtra(PermissionsActivity.REQUESTED_PERMISSIONS, PermissionUtils.PERMISSION_STORAGE);
                    startActivityForResult(intent, PermissionsActivity.PERMISSION_REQUEST_CODE);
                }
            });

        }
    }

    private void startService() {
        mReceiverUnregistered = false;
        IntentFilter intentFilter = new IntentFilter(IndexerService.MSG_DONE);
        registerReceiver(mReceiver, intentFilter);
        Intent intent = getIntent();
        intent.setClass(this, IndexerService.class);
        startService(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionsActivity.PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data.getBooleanExtra(PermissionsActivity.PERMISSION_RESULT, false)) {
                startService();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mReceiverUnregistered)
            unregisterReceiver(mReceiver);
    }
}
