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
