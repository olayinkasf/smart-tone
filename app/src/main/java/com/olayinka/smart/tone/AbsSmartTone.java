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
