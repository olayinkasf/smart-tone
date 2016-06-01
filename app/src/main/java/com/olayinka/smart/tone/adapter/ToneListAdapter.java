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

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortCursorAdapter;
import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.activity.CollectionEditActivity;
import com.olayinka.smart.tone.activity.ImageCacheActivity;
import com.olayinka.smart.tone.activity.MediaGroupActivity;
import com.olayinka.smart.tone.model.MediaItem;
import com.olayinka.smart.tone.service.AppService;

import java.util.ArrayList;

import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 5/3/2015.
 */

public class ToneListAdapter extends DragSortCursorAdapter {


    private static final String QUERY = "select m._id, m._name, m.album_id, m.album_name, m.artist_name, m.is_internal, t.sort_order" +
            " from media m " +
            "inner join tone t on m._id = t.media_id  " +
            "where t.collection_id = ? " +
            "order by t.sort_order";
    private final boolean mRemovable;

    public ToneListAdapter(Context context, long id, boolean removable) {
        super(context, AppSqlHelper.instance(context).getReadableDatabase().rawQuery(QUERY, new String[]{"" + id}), false);
        this.mRemovable = removable;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.tone_item, null);
        view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (!mRemovable)
            view.findViewById(R.id.remove).setVisibility(View.GONE);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        MediaItem mediaItem = new MediaItem(cursor.getLong(0), cursor.getLong(2), cursor.getInt(5));

        ImageView albumArt = (ImageView) view.findViewById(R.id.albumArt);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView albumNameView = (TextView) view.findViewById(R.id.album);
        TextView artistView = (TextView) view.findViewById(R.id.artist);

        titleView.setText(cursor.getString(1));
        albumNameView.setText(cursor.getString(3));
        artistView.setText(cursor.getString(4));

        view.setTag(R.id.mediaItem, mediaItem);

        albumArt.setTag(R.id.mediaItem, mediaItem);
        Uri sArtworkUri = Uri.parse("content://media/" + (mediaItem.getInternal() == 0 ? "external" : "internal") + "/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, mediaItem.getAlbumId());
        ((ImageCacheActivity) context).loadBitmap(uri, albumArt, (int) Utils.pxFromDp(view.getContext(), 50));

        albumArt.setOnTouchListener((View.OnTouchListener) context);
    }


    public void persist(long collectionId, String collectionName) {
        Cursor cursor = getCursor();

        cursor.moveToPosition(-1);
        int count = 0;
        while (cursor.moveToNext()) {
            int listPos = getListPosition(cursor.getPosition());
            if (listPos != REMOVED)
                count++;
        }
        cursor.moveToPosition(-1);
        ArrayList<Long> selection = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            selection.add(-1l);
        while (cursor.moveToNext()) {
            int listPos = getListPosition(cursor.getPosition());
            if (listPos != REMOVED)
                selection.set(listPos, cursor.getLong(0));
        }
        saveCollection(collectionId, collectionName, selection);
        changeCursor(AppSqlHelper.instance(mContext).getReadableDatabase().rawQuery(QUERY, new String[]{"" + collectionId}));
        notifyDataSetChanged();
    }

    @Override
    public void remove(final int wench) {
        new AlertDialog.Builder(mContext)
                .setMessage(mContext.getString(R.string.confirm_delete))
                .setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reallyRemove(wench);
                    }
                }).setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notifyDataSetChanged();
            }
        })
                .show();
    }

    private void reallyRemove(int wench) {
        super.remove(wench);
    }

    private void saveCollection(long collectionId, String collectionName, ArrayList<Long> selection) {
        Intent intent = new Intent(mContext, AppService.class);
        intent.setType(AppService.SAVE_COLLECTION);
        intent.putExtra(MediaGroupActivity.SELECTION, Utils.serialize(selection));
        intent.putExtra(CollectionEditActivity.COLLECTION_ID, collectionId);
        intent.putExtra(CollectionEditActivity.COLLECTION_NAME, collectionName.trim());
        mContext.startService(intent);
    }
}
