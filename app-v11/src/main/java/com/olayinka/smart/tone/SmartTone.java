package com.olayinka.smart.tone;

import com.olayinka.smart.tone.service.NotifAccessibilityService;
import com.olayinka.smart.tone.service.ServiceManager;

/**
 * Created by Olayinka on 5/9/2015.
 */
public class SmartTone extends AbsSmartTone {


    @Override
    protected void startApiServices() {
        ServiceManager.startAlarm(getApplicationContext(), NotifAccessibilityService.class);
    }
}
