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
import android.content.ContentValues;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.adapter.ToneListAdapter;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;

import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 5/10/2015.
 */
public class CollectionPageActivity extends ImageCacheActivity implements View.OnClickListener {

    public static final int GROUP_RETURN_CODE = CollectionEditActivity.GROUP_RETURN_CODE;
    public static final String ART_MEDIA_ITEM = "art.media.item";
    public static final String COLLECTION_ID = "collection.id";

    long mCollectionId;

    ContentValues mCollectionContentValues;
    private ToneListAdapter mAdapter;

    @Override
    protected void onPause() {
        if (mCollectionId != 0 && mCollectionContentValues != null) {
            mAdapter.persist(mCollectionId, mCollectionContentValues.getAsString(Media.CollectionColumns.NAME));
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        //getMenuInflater().inflate(R.menu.collection_page, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Utils.hasLollipop()) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_page);
        setCollection();
        setCollectionArt();
        setListView();
        setEditButtonClickListener();
    }

    private void setEditButtonClickListener() {
        findViewById(R.id.edit).setOnClickListener(this);
        if (isFolderCollection()) findViewById(R.id.edit).setVisibility(View.GONE);
    }

    private void setListView() {
        ListView listView = (ListView) findViewById(R.id.list);
        mAdapter = new ToneListAdapter(this, mCollectionId, !isFolderCollection());
        listView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        setCollection();
        super.onStart();
        mAdapter.getCursor().requery();
        mAdapter.notifyDataSetChanged();
    }

    private void setCollection() {
        mCollectionId = getIntent().getLongExtra(COLLECTION_ID, 0);
        mCollectionContentValues = Media.getCollection(this, mCollectionId);
    }

    private void setCollectionArt() {
        ImageView albumArt = (ImageView) findViewById(R.id.albumArt);
        Utils.squareImageView(this, albumArt);
        MediaItem mediaItem = getIntent().getParcelableExtra(ART_MEDIA_ITEM);
        if (mediaItem != null)
            loadBitmap(Utils.uriForMediaItem(mediaItem), albumArt, Utils.displayDimens(this).widthPixels / 5);
    }

    @Override
    void initToolbar(Toolbar toolbar) {
        ((TextView) toolbar.findViewById(R.id.collectionName)).setText(mCollectionContentValues.getAsString(Media.CollectionColumns.NAME));
        if (isFolderCollection()) {
            toolbar.findViewById(R.id.folderPath).setVisibility(View.VISIBLE);
            ((TextView) toolbar.findViewById(R.id.folderPath)).setText(Media.getFolderPath(this, mCollectionContentValues.getAsLong(Media.CollectionColumns.FOLDER_ID)));
        }
    }

    private boolean isFolderCollection() {
        return mCollectionContentValues.getAsLong(Media.CollectionColumns.FOLDER_ID) > 0;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, CollectionEditActivity.class);
        intent.putExtra(CollectionEditActivity.COLLECTION_ID, mCollectionId);
        startActivity(intent);
    }
}
