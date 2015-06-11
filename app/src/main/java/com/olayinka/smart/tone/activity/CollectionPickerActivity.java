package com.olayinka.smart.tone.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.adapter.CollectionListAdapter;
import com.olayinka.smart.tone.model.MediaItem;
import lib.olayinka.smart.tone.R;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created by olayinka on 5/7/15.
 */
public abstract class CollectionPickerActivity extends ImageCacheActivity implements View.OnClickListener {

    private int mAlbumArtWidth;
    private CollectionListAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_list);
        mAlbumArtWidth = getResources().getDimensionPixelSize(R.dimen.collection_item_art_height);
        setListView();
    }

    private void setListView() {
        mAdapter = new CollectionListAdapter(this, this);
        ((GridView) findViewById(R.id.list)).setAdapter(mAdapter);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    void initToolbar(Toolbar toolbar) {
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.collections);
    }

    public void loadBitmap(RelativeLayout albumArts, long collectionId, HashMap<Long, TreeSet<MediaItem>> itemsMap) {
        TreeSet<MediaItem> items = itemsMap.get(collectionId);

        //default
        for (int i = 0; i < 3; i++) albumArts.getChildAt(i).setVisibility(View.GONE);
        ImageView imageView = (ImageView) albumArts.getChildAt(3);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(params);
        imageView.setImageBitmap(mPlaceHolderBitmap);

        if (items == null || items.size() == 0) return;

        switch (items.size()) {
            case 1: {
                MediaItem mediaItem = items.last();
                imageView = (ImageView) albumArts.getChildAt(3);
                loadBitmap(Utils.uriForMediaItem(mediaItem), imageView, mAlbumArtWidth);
            }
            break;
            default: {
                for (int i = 0; i < 4 - items.size(); i++) albumArts.getChildAt(i).setVisibility(View.GONE);
                for (int i = 4 - items.size(); i < 4; i++) albumArts.getChildAt(i).setVisibility(View.VISIBLE);
                int i = 0;
                for (MediaItem mediaItem : items) {
                    int pos = 4 - items.size() + i;
                    imageView = (ImageView) albumArts.getChildAt(pos);
                    params = new RelativeLayout.LayoutParams(mAlbumArtWidth, mAlbumArtWidth);
                    params.setMargins(mAlbumArtWidth - (i * mAlbumArtWidth) / (items.size() - 1), 0, 0, 0);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    loadBitmap(Utils.uriForMediaItem(mediaItem), imageView, mAlbumArtWidth);
                    i++;
                }
            }
            break;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.getCursor().requery();
        mAdapter.notifyDataSetChanged();
    }

}
