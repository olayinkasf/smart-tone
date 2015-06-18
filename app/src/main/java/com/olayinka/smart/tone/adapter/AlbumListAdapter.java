package com.olayinka.smart.tone.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.activity.CollectionEditActivity;
import com.olayinka.smart.tone.activity.ImageCacheActivity;
import com.olayinka.smart.tone.activity.MediaGroupActivity;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;
import lib.olayinka.smart.tone.R;

import java.util.Set;

/**
 * Created by Olayinka on 5/3/2015.
 */
public class AlbumListAdapter extends CursorAdapter implements  View.OnClickListener {

    private Set<Long> mSelection;

    public AlbumListAdapter(Context context, String table, Set<Long> selected) {
        super(context, AppSqlHelper.instance(context).getReadableDatabase()
                .query(table, new String[]{"*"}, null, null, null, null, Media.Columns.NAME)
                , false);
        mSelection = selected;
    }

    String getTable() {
        return Media.Album.TABLE;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.album_item, null);
        view.setOnClickListener(this);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        MediaItem albumItem = getItemForAlbumArt(cursor);

        view.setTag(R.id.mediaItem, albumItem);
        ImageView albumArt = (ImageView) view.findViewById(R.id.albumArt);
        TextView albumName = (TextView) view.findViewById(R.id.albumName);
        TextView artistName = (TextView) view.findViewById(R.id.artistName);

        albumName.setText(cursor.getString(2));
        artistName.setText(cursor.getString(3));

        albumArt.setTag(R.id.mediaItem, albumItem);
        ((ImageCacheActivity) albumArt.getContext()).loadBitmap(Utils.uriForMediaItem(albumItem), albumArt, (int) Utils.pxFromDp(view.getContext(), 70));

    }

    public MediaItem getItemForAlbumArt(Cursor cursor) {
        return new MediaItem(cursor.getLong(0), cursor.getLong(1), cursor.getInt(4));
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), MediaGroupActivity.class);
        intent.putExtra(MediaGroupActivity.SELECTION, Utils.serialize(mSelection));
        intent.putExtra(MediaGroupActivity.MEDIA_ITEM, v.getTag(R.id.mediaItem).toString());
        intent.putExtra(MediaGroupActivity.TABLE, getTable());
        if (Utils.hasLollipop()) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation((Activity) v.getContext(), v, v.getContext().getString(R.string.transition_media_group));
            ((Activity) v.getContext()).startActivityForResult(intent, CollectionEditActivity.GROUP_RETURN_CODE, options.toBundle());
        } else ((Activity) v.getContext()).startActivityForResult(intent, CollectionEditActivity.GROUP_RETURN_CODE);
    }
}
