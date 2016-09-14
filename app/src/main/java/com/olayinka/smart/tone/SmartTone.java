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
import android.content.Intent;

import com.olayinka.smart.tone.service.SmartToneService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Olayinka on 5/9/2015.
 */
public class SmartTone extends Application {

    @Override
    public void onCreate() {
        monitorLog();
        AppLogger.wtf(this, "onCreate", "Launching main process.");
        super.onCreate();
        startService(new Intent(this, SmartToneService.class));
    }

    private void monitorLog() {
        File logFile = new File(getFileStreamPath("smart.tone.log").getAbsolutePath());
        File backUpLogFile = new File(getFileStreamPath("smart.tone.bck.log").getAbsolutePath());
        if (logFile.length() <= 1048576 * 5) {
            try {
                Utils.copyFile(logFile, backUpLogFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(logFile);
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
