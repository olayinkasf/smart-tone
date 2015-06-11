package com.olayinka.smart.tone.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.activity.ImageCacheActivity;
import com.olayinka.smart.tone.model.MediaItem;
import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 5/3/2015.
 */

public class ToneListAdapter extends CursorAdapter {

    public ToneListAdapter(Context context, long id) {
        super(context, AppSqlHelper.instance(context).getReadableDatabase()
                .rawQuery(
                        "select *  from media m inner join tone t  on m._id = t.media_id  where t.collection_id = ?",
                        new String[]{"" + id}
                ), false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.tone_item, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        MediaItem mediaItem = new MediaItem(cursor.getLong(0), cursor.getLong(4), cursor.getInt(10));

        ImageView albumArt = (ImageView) view.findViewById(R.id.albumArt);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView albumNameView = (TextView) view.findViewById(R.id.album);
        TextView artistView = (TextView) view.findViewById(R.id.artist);

        titleView.setText(cursor.getString(2));
        albumNameView.setText(cursor.getString(5));
        artistView.setText(cursor.getString(6));

        view.setTag(R.id.mediaItem, mediaItem);

        albumArt.setTag(R.id.mediaItem, mediaItem);
        Uri sArtworkUri = Uri.parse("content://media/" + (mediaItem.getInternal() == 0 ? "external" : "internal") + "/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, mediaItem.getAlbumId());
        ((ImageCacheActivity) albumArt.getContext()).loadBitmap(uri, albumArt, (int) Utils.pxFromDp(view.getContext(), 50));

        albumArt.setOnTouchListener((View.OnTouchListener) context);
    }

}
