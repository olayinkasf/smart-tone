package com.olayinka.smart.tone.service;

import android.content.Intent;

/**
 * Created by ofolorunso on 11/09/16.
 */
public abstract class SmartToneStub {

    protected final SmartToneService mService;

    public SmartToneStub(SmartToneService service) {
        this.mService = service;
    }

    abstract void handleIntent(Intent intent);

    public Intent action(String... actions) {
        return mService.action(actionFilter(), actions);
    }

     abstract String actionFilter();

    public abstract void selfDestroy();
}
