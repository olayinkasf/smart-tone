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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.olayinka.smart.tone.PermissionUtils;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.billing.utils.IabHelper;
import com.olayinka.smart.tone.billing.utils.IabResult;
import com.olayinka.smart.tone.billing.utils.Inventory;
import com.olayinka.smart.tone.billing.utils.Purchase;
import lib.olayinka.smart.tone.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Olayinka on 5/30/2015.
 */
public class AboutAppActivity extends AppCompatActivity {

    IabHelper mHelper;
    private String mDonationKey = "sku.donation";


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
        } else if (i == R.id.rate) {
            rate();
        } else if (i == R.id.contactHelp) {
            contact();
        } else if (i == R.id.share) {
            share();
        }
        return super.onOptionsItemSelected(item);
    }

    private void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share) + " https://play.google.com/store/apps/details?id=" + getPackageName());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void contact() {
        String uriString = ("mailto:" + "smart.tone.app@gmail.com" + "?subject=" + "SmartTone Support" + "&body=").replace(" ", "%20");
        startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.parse(uriString)), getString(R.string.contact_dev)));
    }

    private void rate() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Utils.toast(this, R.string.market_fail);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.about_app, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String mBase64EncodedPublicKey;
        try {
            mBase64EncodedPublicKey = getKey();
        } catch (IOException e) {
            throw new RuntimeException();
        }

        final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                //noinspection StatementWithEmptyBody
                if (result.isFailure()) {
                    //TODO something to do if result failed, nothing for now :(
                } else if (purchase != null && purchase.getPurchaseState() == 0 && purchase.getSku().equals(mDonationKey))
                    setDonation();
            }
        };

        final IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                //noinspection StatementWithEmptyBody
                if (result.isFailure()) {
                    //TODO something to do if result failed, nothing for now :(
                } else {
                    Purchase purchase = inventory.getPurchase(mDonationKey);
                    activateDonation();
                    if (purchase != null && purchase.getPurchaseState() == 0 && purchase.getSku().equals(mDonationKey))
                        setDonation();
                }
            }
        };

        setContentView(R.layout.about_app);
        setActionBar();

        View itemView = findViewById(R.id.appVersion);
        TextView titleView = (TextView) itemView.findViewById(R.id.title);
        TextView textView = (TextView) itemView.findViewById(R.id.text);
        titleView.setText(R.string.app_version);
        textView.setText(getAppVersion());

        itemView = findViewById(R.id.permissions);
        titleView = (TextView) itemView.findViewById(R.id.title);
        textView = (TextView) itemView.findViewById(R.id.text);
        titleView.setText(R.string.permissions);
        textView.setText(String.format(getString(R.string.permissions_granted), PermissionUtils.countGrantedPermissions(this), PermissionUtils.PERMISSIONS.length));
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), PermissionsActivity.class));
            }
        });

        deactivateDonation();
        itemView = findViewById(R.id.donate);
        titleView = (TextView) itemView.findViewById(R.id.title);
        textView = (TextView) itemView.findViewById(R.id.text);
        titleView.setText(R.string.donate);
        textView.setText(R.string.donate_text);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHelper.launchPurchaseFlow(AboutAppActivity.this, mDonationKey, 10001,
                        mPurchaseFinishedListener, mDonationKey);
            }
        });

        itemView = findViewById(R.id.legal);
        titleView = (TextView) itemView.findViewById(R.id.title);
        textView = (TextView) itemView.findViewById(R.id.text);
        textView.setVisibility(View.GONE);
        titleView.setText(R.string.legal);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), LegalActivity.class));
            }
        });

        ((TextView) findViewById(R.id.aboutAppText)).setText(Utils.getRawString(this, R.raw.about_app));

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        if (getPackageManager().queryIntentServices(serviceIntent, 0) != null) {
            // compute your public key and store it in base64EncodedPublicKey
            mHelper = new IabHelper(this, mBase64EncodedPublicKey);
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        Utils.toast(AboutAppActivity.this, R.string.billing_error);
                    } else {
                        mHelper.queryInventoryAsync(mGotInventoryListener);
                    }
                }
            });
        } else {
            Utils.toast(AboutAppActivity.this, R.string.billing_missing);
        }
    }

    private void setDonation() {
        View itemView = findViewById(R.id.donate);
        TextView titleView = (TextView) itemView.findViewById(R.id.title);
        TextView textView = (TextView) itemView.findViewById(R.id.text);
        textView.setVisibility(View.GONE);
        titleView.setText(R.string.donated);
        itemView.setOnClickListener(null);
    }

    private void deactivateDonation() {
        View itemView = findViewById(R.id.donate);
        TextView titleView = (TextView) itemView.findViewById(R.id.title);
        itemView.setEnabled(false);
        titleView.setEnabled(false);
    }

    private void activateDonation() {
        View itemView = findViewById(R.id.donate);
        TextView titleView = (TextView) itemView.findViewById(R.id.title);

        itemView.setEnabled(true);
        titleView.setEnabled(true);
    }

    private String getKey() throws IOException {
        //39 431
        InputStream is = getResources().openRawResource(R.raw.salt);
        Scanner scanner = new Scanner(new InputStreamReader(is));
        int[] salt = new int[39];
        for (int i = 0; i < salt.length; i++) {
            salt[i] = scanner.nextInt();
        }
        scanner.close();

        is = getResources().openRawResource(R.raw.key);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        line = reader.readLine();
        reader.close();

        if (line.length() != 431) throw new RuntimeException();

        char[] codeCache = line.toCharArray(), code = new char[431 - 39];
        ArrayList<Character> codeList = new ArrayList<>(codeCache.length);
        for (char c : codeCache) codeList.add(c);
        for (int i = salt.length - 1; i >= 0; i--) codeList.remove(salt[i]);

        if (code.length != codeList.size()) throw new RuntimeException();

        for (int i = 0; i < codeList.size(); i++) code[i] = codeList.get(i);

        return String.valueOf(code);
    }

    private String getAppVersion() {
        Map<String, String> varMap = Utils.VAR_MAP;
        return getString(R.string.app_name) + "\n"
                + "Version: " + "v" + varMap.get("version.name") + "\n"
                + "Build: " + varMap.get("build.number") + "x" + varMap.get("build.time") + "-" + varMap.get("build.type");
    }


    private void setActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        toolbar.setTitle(R.string.about_app_title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mHelper.handleActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }
}
