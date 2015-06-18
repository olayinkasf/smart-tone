package com.olayinka.smart.tone.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.olayinka.smart.tone.AbsSmartTone;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.wtf("onReceive", "" + context.toString());
        ((AbsSmartTone) context.getApplicationContext()).startServices();
    }
}
