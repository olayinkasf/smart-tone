package com.olayinka.smart.tone.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageView;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.adapter.CollectionListAdapter;
import com.olayinka.smart.tone.model.MediaItem;
import lib.olayinka.smart.tone.R;

import java.util.TreeSet;

/**
 * Created by Olayinka on 5/8/2015.
 */
public class CollectionToManageActivity extends CollectionPickerActivity {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Utils.hasLollipop()) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View v) {
        GridView gridView = (GridView) findViewById(R.id.list);
        CollectionListAdapter adapter = (CollectionListAdapter) gridView.getAdapter();
        MediaItem mediaItem = null;
        long collectionId = (long) v.getTag(R.id.collectionItem);
        TreeSet<MediaItem> items = adapter.getItemsForCollection((long) v.getTag(R.id.collectionItem));
        if (items != null && items.size() > 0) {
            mediaItem = items.last();
        }
        ImageView imageView = (ImageView) v.findViewById(R.id.transitionImage);
        Intent intent = new Intent(this, CollectionPageActivity.class);
        intent.putExtra(CollectionPageActivity.ART_MEDIA_ITEM, mediaItem == null ? null : mediaItem.toString());
        intent.putExtra(CollectionPageActivity.COLLECTION_ID, collectionId);
        if (Utils.hasLollipop()) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageView, getString(R.string.transition_collection_page));
            ((Activity) v.getContext()).startActivityForResult(intent, CollectionPageActivity.GROUP_RETURN_CODE, options.toBundle());
        } else ((Activity) v.getContext()).startActivityForResult(intent, CollectionPageActivity.GROUP_RETURN_CODE);
        //finish();
    }
}
