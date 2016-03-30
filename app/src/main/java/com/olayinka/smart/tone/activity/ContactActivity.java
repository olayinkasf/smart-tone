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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.olayinka.smart.tone.AppLogger;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.PermissionUtils;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.task.AsyncTask;
import lib.olayinka.smart.tone.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static com.olayinka.smart.tone.Utils.getExternalStorageDir;

/**
 * Created by olayinka on 6/8/15.
 */
public class ContactActivity extends AppCompatActivity {

    private static final String EMAIL = "smart.tone.app@gmail.com";
    private ProgressDialog mDialog;
    private File mLogFile;
    private boolean mAttachLogFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log);
        setActionBar();

        if (!getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE).getBoolean(AppSettings.LOG_APP_ACTIVITY, false)) {
            sendSimpleContact();
            return;
        }

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(getString(R.string.ask_attach_log_file))
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAttachLogFile = false;
                        contact();
                    }
                })
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAttachLogFile = true;
                        contact();
                    }
                })
                .show();
    }

    private void contact() {
        if (mAttachLogFile) {
            mDialog = new ProgressDialog(this);
            mDialog.setCancelable(false);
            mDialog.setMessage(getString(R.string.collecting_log_files));
            mDialog.show();

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {

                    AppLogger.pause();
                    try {
                        getSystemLog();
                    } catch (IOException e) {
                        e.printStackTrace();
                        //Utils.toast(getApplicationContext(), "Error while dumping log file.");
                    }
                    AppLogger.resume();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    sendLogFile();
                }
            }.execute();
        } else {
            sendSimpleContact();
        }

    }

    private void sendSimpleContact() {
        String uriString = ("mailto:" + "smart.tone.app@gmail.com" + "?subject=" + "SmartTone Support" + "&body=").replace(" ", "%20");
        startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.parse(uriString)), getString(R.string.contact_dev)));
        finish();
    }

    private void sendLogFile() {
        if (mDialog != null) mDialog.dismiss();

        if (mLogFile == null) {
            Utils.toast(this, "No log file found on the system.");
            sendSimpleContact();
            finish();
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {EMAIL};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mLogFile));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Application Log");
        startActivity(Intent.createChooser(emailIntent, "Send application log using ..."));
        finish();
    }

    private void getSystemLog() throws IOException {

        File logFile = new File(getFileStreamPath("smart.tone.log").getAbsolutePath());
        File backUpLogFile = new File(getFileStreamPath("smart.tone.bck.log").getAbsolutePath());

        File tmpLogFile = getExternalStorageDir("log", System.currentTimeMillis() + ".log");

        if (tmpLogFile == null) {
            return;
        }

        mLogFile = tmpLogFile;

        try {
            for (File file : tmpLogFile.getParentFile().listFiles()) file.delete();
        } catch (Throwable ignored) {
        }

        try {
            Utils.copyFile(backUpLogFile, tmpLogFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.appendFile(logFile, tmpLogFile);
        dumpPreferences(tmpLogFile);
    }

    public void dumpPreferences(File output) throws IOException {
        SharedPreferences prefs = getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE);
        BufferedWriter writer = new BufferedWriter(new FileWriter(output, true));
        writer.write("Dumping shared preferences and permissions...\n");
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            Object val = entry.getValue();
            if (val == null) {
                writer.write(String.format("%s :\t <null>%n", entry.getKey()));
            } else {
                writer.write(String.format("%s :\t %s (%s)%n", entry.getKey(), String.valueOf(val), val.getClass()
                        .getSimpleName()));
            }
        }

        boolean[] perms = PermissionUtils.getAllPermissions(getApplicationContext());
        for (int i = 0; i < perms.length; i++) {
            writer.write(PermissionUtils.PERMISSIONS[i] + " :\t " + String.valueOf(perms[i]));
            writer.write("\n");
        }

        writer.write("Dump complete\n");
        writer.flush();
        writer.close();
    }

    private void setActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.contact_dev);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (mDialog != null) mDialog.dismiss();
        super.onPause();
    }

}
