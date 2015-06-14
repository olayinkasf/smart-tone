package com.olayinka.smart.tone.activity;

import android.animation.*;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.adapter.CollectionListAdapter;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;
import lib.olayinka.smart.tone.R;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created by olayinka on 5/7/15.
 */
public abstract class CollectionPickerActivity extends ImageCacheActivity implements View.OnClickListener, View.OnLongClickListener {

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
        GridView gridView = ((GridView) findViewById(R.id.list));
        if (!alreadyGotIt()) {
            final View header = findViewById(R.id.longPressHeader);
            header.setVisibility(View.VISIBLE);
            header.findViewById(R.id.got_it).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotIt(v.getContext(), header);
                }
            });

        }
        gridView.setAdapter(mAdapter);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    void initToolbar(Toolbar toolbar) {
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.collections);
    }

    private boolean alreadyGotIt() {
        return getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE)
                .getBoolean(AppSettings.GOT_IT_LONG_PRESS, false);
    }

    private void gotIt(Context context, final View header) {
        context.getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE)
                .edit().putBoolean(AppSettings.GOT_IT_LONG_PRESS, true).apply();

        final int height = header.getMeasuredHeight();
        final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) header.getLayoutParams();

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f).setDuration(300);

        ValueAnimator heightAnimator = ValueAnimator.ofInt(height, 0).setDuration(400);
        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                layoutParams.height = (Integer) valueAnimator.getAnimatedValue();
                header.setLayoutParams(layoutParams);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(alphaAnimator, heightAnimator);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                header.setVisibility(View.GONE);
            }
        });

        set.start();
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

    @Override
    public boolean onLongClick(View v) {
        final long collectionId = (long) v.getTag(R.id.collectionId);
        final String collectionName = (String) v.getTag(R.id.collectionName);
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.delete) + " " + collectionName + "?")
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppSettings.deleteCheck(CollectionPickerActivity.this, collectionId);
                        Media.deleteCollection(CollectionPickerActivity.this, collectionId);
                        Utils.toast(CollectionPickerActivity.this, getString(R.string.collection) + " " + collectionName + " " + getString(R.string.deleted));
                        mAdapter.changeCursor(CollectionPickerActivity.this);
                    }
                }).show();
        return true;
    }
}
