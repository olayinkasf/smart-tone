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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.olayinka.smart.tone.Utils;
import lib.olayinka.smart.tone.R;

/**
 * Created by olayinka on 6/8/15.
 */
public class LegalActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legal);
        setActionBar();

        View itemView;
        TextView titleView;
        TextView textView;

        itemView = findViewById(R.id.privacyPolicy);
        titleView = (TextView) itemView.findViewById(R.id.title);
        textView = (TextView) itemView.findViewById(R.id.text);
        textView.setVisibility(View.GONE);
        titleView.setText(R.string.privacy_policy);
        itemView.setOnClickListener(this);

        itemView = findViewById(R.id.openSourceLicense);
        titleView = (TextView) itemView.findViewById(R.id.title);
        textView = (TextView) itemView.findViewById(R.id.text);
        textView.setVisibility(View.GONE);
        titleView.setText(R.string.open_source_license);
        itemView.setOnClickListener(this);

        itemView = findViewById(R.id.contactHelp);
        titleView = (TextView) itemView.findViewById(R.id.title);
        textView = (TextView) itemView.findViewById(R.id.text);
        textView.setVisibility(View.GONE);
        titleView.setText(R.string.contact);
        itemView.setOnClickListener(this);

    }

    private void setActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.legal);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.privacyPolicy) {
            try {
                String mDownloadLink = "https://smarttone.github.io/privacy-policy/";
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mDownloadLink));
                startActivity(myIntent);
            } catch (ActivityNotFoundException e) {
                Utils.toast(this, R.string.install_browser);
                e.printStackTrace();
            }
        } else if (v.getId() == R.id.openSourceLicense) {
            startActivity(new Intent(v.getContext(), OpenSourceLibraries.class));
        } else if (v.getId() == R.id.contactHelp) {
            String uriString = ("mailto:" + "smart.tone.app@gmail.com" + "?subject=" + "SmartTone Support" + "&body=").replace(" ", "%20");
            startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.parse(uriString)), getString(R.string.contact_dev)));
        }
    }
}
