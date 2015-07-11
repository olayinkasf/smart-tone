package com.olayinka.smart.tone.activity;

import android.annotation.TargetApi;
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
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.adapter.ToneListAdapter;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;
import lib.olayinka.smart.tone.R;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Olayinka on 5/10/2015.
 */
public class CollectionPageActivity extends ImageCacheActivity implements View.OnClickListener {

    public static final int GROUP_RETURN_CODE = CollectionEditActivity.GROUP_RETURN_CODE;
    public static final String ART_MEDIA_ITEM = "art.media.item";
    public static final String COLLECTION_ID = "collection.id";

    long mCollectionId;

    JSONObject mCollectionObject;
    private ToneListAdapter mAdapter;

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
    }

    private void setListView() {
        ListView listView = (ListView) findViewById(R.id.list);
        mAdapter = new ToneListAdapter(this, mCollectionId);
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
        try {
            mCollectionObject = Media.getCollection(this, mCollectionId);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void setCollectionArt() {
        ImageView albumArt = (ImageView) findViewById(R.id.albumArt);
        Utils.squareImageView(this, albumArt);
        String mediaItemString = getIntent().getStringExtra(ART_MEDIA_ITEM);
        if (mediaItemString != null) try {
            MediaItem mediaItem = MediaItem.fromJSONObject(new JSONObject(mediaItemString));
            loadBitmap(Utils.uriForMediaItem(mediaItem), albumArt, Utils.displayDimens(this).widthPixels / 5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    void initToolbar(Toolbar toolbar) {
        try {
            toolbar.setTitle(mCollectionObject.getString(Media.CollectionColumns.NAME));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, CollectionEditActivity.class);
        intent.putExtra(CollectionEditActivity.COLLECTION_ID, mCollectionId);
        startActivity(intent);
    }
}
