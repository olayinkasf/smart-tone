package com.olayinka.smart.tone;

import android.app.Application;
import android.util.Log;
import com.olayinka.smart.tone.service.RingtoneTelephonyService;
import com.olayinka.smart.tone.service.ServiceManager;
import com.olayinka.smart.tone.service.ShuffleService;

/**
 * Created by Olayinka on 5/9/2015.
 */
public abstract class AbsSmartTone extends Application {

    @Override
    public void onCreate() {
        Log.wtf("onCreate", "Launching main process.");
        super.onCreate();
        startServices();
    }

    public void startServices() {
        ServiceManager.startAlarm(this, RingtoneTelephonyService.class);
        ServiceManager.startAlarm(this, ShuffleService.class);
        startApiServices();
    }
    
    protected abstract void startApiServices();
}
