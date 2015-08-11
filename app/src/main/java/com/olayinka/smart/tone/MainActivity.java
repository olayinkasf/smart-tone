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

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import com.olayinka.smart.tone.activity.AnotherMenuActivity;
import com.olayinka.smart.tone.service.IndexerService;
import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 4/19/2015.
 */
public class MainActivity extends Activity {
    boolean mReceiverUnregistered;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        //if (BuildConfig.DEBUG)
            //getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE)
            //        .edit().clear().commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.wtf("onStart", "mReceiver");
                unregisterReceiver(this);
                mReceiverUnregistered = true;
                intent = new Intent();
                intent.setComponent(new ComponentName(MainActivity.this, AnotherMenuActivity.CONCRETE));
                startActivity(intent);
                finish();
            }
        };
        IntentFilter intentFilter = new IntentFilter(IndexerService.MSG_DONE);
        registerReceiver(mReceiver, intentFilter);

        Intent intent = getIntent();
        intent.setClass(this, IndexerService.class);
        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mReceiverUnregistered)
            unregisterReceiver(mReceiver);
    }
}
