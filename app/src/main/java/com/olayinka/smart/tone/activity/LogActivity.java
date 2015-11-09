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

package com.olayinka.smart.tone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import com.olayinka.smart.tone.AppLogger;
import com.olayinka.smart.tone.Utils;
import lib.olayinka.smart.tone.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by olayinka on 6/8/15.
 */
public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log);
        setActionBar();

        WebView content = (WebView) findViewById(R.id.appLog);
        content.getSettings().setLoadWithOverviewMode(true);
        content.getSettings().setUseWideViewPort(true);

        String mime = "text/html";
        String encoding = "utf-8";
        content.loadDataWithBaseURL(null, Utils.getRawString(this, R.raw.log).replace("%s", getSystemLog()), mime, encoding, null);
    }

    private String getSystemLog() {

        StringBuilder contentString = new StringBuilder();

        File logFile = new File(getFileStreamPath("smart.tone.log").getAbsolutePath());
        File backUpLogFile = new File(getFileStreamPath("smart.tone.bck.log").getAbsolutePath());
        try {
            contentString.append(Utils.readFile(backUpLogFile));
        } catch (IOException e) {
            AppLogger.wtf(this, "onCreate", e);
        }
        try {
            contentString.append(Utils.readFile(logFile)).append("\n");
        } catch (IOException e) {
            AppLogger.wtf(this, "onCreate", e);
        }
        return contentString.toString();
    }

    private void setActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.app_log);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
        } else if (i == R.id.sendLog) {
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("text/html");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"smart.tone.app@gmail.com"});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Application Log");
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getSystemLog());
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_app_log)));
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.log, menu);
        return true;
    }
}
