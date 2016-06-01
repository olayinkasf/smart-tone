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

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.MainActivity;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.adapter.MediaPagerAdapter;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.OrderedMediaSet;
import com.olayinka.smart.tone.service.AppService;
import com.olayinka.smart.tone.service.IndexerService;
import com.olayinka.smart.tone.widget.SlidingTabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashSet;

import lib.olayinka.smart.tone.R;

/**
 * Created by olayinka on 4/30/15.
 */
public class CollectionEditActivity extends ImageCacheActivity {

    public static final int GROUP_RETURN_CODE = 16916;
    public static final String COLLECTION_ID = "collection.id";
    public static final String COLLECTION_NAME = "collection.name";
    private long mCollectionId;
    private String mCollectionName;
    private OrderedMediaSet<Long> mSelection;

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.exit_warning)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save) {
            if (mSelection.isEmpty()) {
                Utils.toast(this, R.string.empty_selection);
            } else {
                final EditText textView = (EditText) findViewById(R.id.collectionName);
                if (textView.getText().toString().trim().isEmpty()) {
                    Utils.toast(this, R.string.name_empty);
                    textView.setFocusableInTouchMode(true);
                    textView.requestFocus();
                    final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.save_collection)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    saveCollection(textView.getText().toString());
                                }
                            }).show();
                }
            }
        } else if (id == R.id.search) {
            Intent intent = new Intent(this, SearchMusicActivity.class);
            intent.putExtra(SearchMusicActivity.SELECTION, Utils.serialize(mSelection.getList()));
            startActivityForResult(intent, GROUP_RETURN_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveCollection(String name) {
        final long fauxId = System.currentTimeMillis();
        Intent intent = new Intent(this, AppService.class);
        intent.setType(AppService.SAVE_COLLECTION);
        intent.putExtra(MediaGroupActivity.SELECTION, Utils.serialize(mSelection.getList()));
        intent.putExtra(COLLECTION_ID, mCollectionId);
        intent.putExtra(AppService.FAUX_ID, fauxId);
        intent.putExtra(COLLECTION_NAME, name.trim());
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.saving_collection));
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                unregisterReceiver(this);
                if (fauxId != intent.getLongExtra(AppService.FAUX_ID, 0))
                    throw new RuntimeException();
                dialog.dismiss();
                finish();
            }
        };
        registerReceiver(receiver, new IntentFilter(AppService.SAVE_COLLECTION));
        dialog.show();
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.collection_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Utils.hasLollipop()) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_edit);
        setCollection();
        setPager();
    }

    private void setPager() {
        if (!AppSqlHelper.hasData(this, Media.TABLE)) {
            final Intent intent = new Intent(this, MainActivity.class);
            new AlertDialog.Builder(this)
                    .setMessage(R.string.empty_reindex)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra(IndexerService.FORCE_INDEX, true);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        MediaPagerAdapter mPagerAdapter = new MediaPagerAdapter(mSelection, getResources().getStringArray(R.array.tabs_header));
        mViewPager.setAdapter(mPagerAdapter);
        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        mViewPager.setCurrentItem(1, false);
    }

    private void setCollection() {
        mCollectionId = getIntent().getLongExtra(COLLECTION_ID, 0);
        mCollectionName = "";
        try {
            JSONObject collectionObject = Media.getCollection(this, mCollectionId);
            JSONArray selectionJsonArray = Media.getTones(this, mCollectionId);
            mCollectionName = collectionObject.optString(Media.CollectionColumns.NAME, "");
            mSelection = new OrderedMediaSet<>(selectionJsonArray.length());
            for (int i = 0; i < selectionJsonArray.length(); i++) {
                mSelection.add(selectionJsonArray.getLong(i));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initToolbar(Toolbar toolbar) {
        ((TextView) toolbar.findViewById(R.id.collectionName))
                .setText(mCollectionName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GROUP_RETURN_CODE) {
            try {
                mSelection.clear();
                JSONArray selectionJsonArray = new JSONArray(data.getStringExtra(MediaGroupActivity.SELECTION));
                LinkedHashSet<Long> tmpCollection = new LinkedHashSet<>(selectionJsonArray.length());
                for (int i = 0; i < selectionJsonArray.length(); i++) {
                    tmpCollection.add(selectionJsonArray.getLong(i));
                }
                mSelection.addAll(tmpCollection);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}