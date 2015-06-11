package com.olayinka.smart.tone.activity;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.adapter.MediaListAdapter;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;
import lib.olayinka.smart.tone.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Olayinka on 5/2/2015.
 */
public class MediaGroupActivity extends ImageCacheActivity {

    public static final String SELECTION = "media.selection";
    public static final String MEDIA_ITEM = "media.item";
    public static final String TABLE = "media.table";
    MediaItem mMediaItem;
    long[] mMedias;
    private Set<Long> mSelection;
    private MediaListAdapter mAdapter;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Utils.hasLollipop()) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_page);
        try {
            setSelection();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        initViews();
        setToggleSelectAllButtonClickListener();
    }

    private void setToggleSelectAllButtonClickListener() {
        findViewById(R.id.toggleSelectAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean all = true;
                for (long id : mMedias) {
                    if (!mSelection.contains(id)) {
                        all = false;
                        break;
                    }
                }
                if (all) for (long id : mMedias) mSelection.remove(id);
                else for (long id : mMedias) mSelection.add(id);
                ((MediaListAdapter) ((ListView) findViewById(R.id.list)).getAdapter()).notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    void initToolbar(Toolbar toolbar) {
        Cursor cursor = AppSqlHelper.instance(this).getReadableDatabase()
                .query(getIntent().getStringExtra(TABLE), new String[]{"*"}, Media.Columns._ID + Media.EQUALS, new String[]{"" + mMediaItem.getId()}, null, null, null);
        cursor.moveToNext();
        TextView albumName = (TextView) toolbar.findViewById(R.id.albumName);
        TextView artistName = (TextView) toolbar.findViewById(R.id.artistName);
        albumName.setText(cursor.getString(2));
        artistName.setText(cursor.getString(3));
        cursor.close();
    }

    private void setSelection() throws JSONException {
        JSONArray jsonArray = new JSONArray(getIntent().getStringExtra(SELECTION));
        mSelection = new HashSet<>(1000);
        for (int i = 0; i < jsonArray.length(); i++) {
            mSelection.add(jsonArray.getLong(i));
        }
        mMediaItem = MediaItem.fromJSONObject(new JSONObject(getIntent().getStringExtra(MEDIA_ITEM)));
        ImageView albumArt = (ImageView) findViewById(R.id.albumArt);
        albumArt.setTag(R.id.mediaItem, mMediaItem);
        Uri sArtworkUri = Uri.parse("content://media/" + (mMediaItem.getInternal() == 0 ? "external" : "internal") + "/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, mMediaItem.getAlbumId());
        loadBitmap(uri, albumArt, Utils.displayDimens(this).widthPixels / 5);
    }

    private void initViews() {
        ListView listView = (ListView) findViewById(R.id.list);
        String selection = null;
        switch (getIntent().getStringExtra(TABLE)) {
            case Media.Folder.TABLE:
                selection = getIntent().getStringExtra(TABLE) + Media.Columns._ID + " = " + mMediaItem.getId();
                break;
            case Media.Album.TABLE:
                selection = getIntent().getStringExtra(TABLE) + Media.Columns._ID + " = " + mMediaItem.getAlbumId() + Media.AND
                        + Media.Columns.IS_INTERNAL + " = " + mMediaItem.getInternal();
                break;
        }

        mAdapter = new MediaListAdapter(this, selection, mSelection);
        listView.setAdapter(new MediaListAdapter(this, selection, mSelection));
        Cursor cursor = AppSqlHelper.instance(this).getReadableDatabase()
                .query(Media.TABLE, new String[]{Media.Columns._ID}, selection, null, null, null, null);
        mMedias = new long[cursor.getCount()];
        for (int i = 0; i < mMedias.length; i++) {
            cursor.moveToNext();
            mMedias[i] = cursor.getLong(0);
        }
        cursor.close();
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(SELECTION, Utils.serialize(mSelection));
        setResult(RESULT_OK, data);
        super.finish();
    }

}
