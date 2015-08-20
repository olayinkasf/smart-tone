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
import com.olayinka.smart.tone.model.OrderedMediaSet;
import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 5/3/2015.
 */
public class AlbumListAdapter extends CursorAdapter implements  View.OnClickListener {

    private OrderedMediaSet<Long> mSelection;

    public AlbumListAdapter(Context context, String table, OrderedMediaSet<Long> selected) {
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
        intent.putExtra(MediaGroupActivity.SELECTION, Utils.serialize(mSelection.getList()));
        intent.putExtra(MediaGroupActivity.MEDIA_ITEM, v.getTag(R.id.mediaItem).toString());
        intent.putExtra(MediaGroupActivity.TABLE, getTable());
        if (Utils.hasLollipop()) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation((Activity) v.getContext(), v, v.getContext().getString(R.string.transition_media_group));
            ((Activity) v.getContext()).startActivityForResult(intent, CollectionEditActivity.GROUP_RETURN_CODE, options.toBundle());
        } else ((Activity) v.getContext()).startActivityForResult(intent, CollectionEditActivity.GROUP_RETURN_CODE);
    }
}
