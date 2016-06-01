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

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.activity.ImageCacheActivity;
import com.olayinka.smart.tone.model.ListenableHashSet;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;
import com.olayinka.smart.tone.task.MediaPlayBackTask;

import java.util.Collection;
import java.util.Set;

import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 5/3/2015.
 */

public class MediaListAdapter extends CursorAdapter implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, ListenableHashSet.HashSetListener<Long> {


    public static final String SELECTION_ALL = null;
    public static final String SELECTION_RINGTONE = Media.Columns.IS_NOTIFICATION + " == 0";
    public static final String SELECTION_NOTIFICATION = Media.Columns.IS_NOTIFICATION + " != 0";
    public static final String SELECTION_SELECTED = Media.Columns._ID + " IN (%s)";
    public static final String SELECTION_NONE = Media.Columns._ID + " = 0";
    public static final String SELECTION_SEARCH = Media.Columns.NAME + Media.LIKE + Media.OR
            + Media.Columns.ALBUM_NAME + Media.LIKE + Media.OR
            + Media.Columns.ARTIST_NAME + Media.LIKE;

    protected Set<Long> mSelection;

    public MediaListAdapter(Context context, String selection, Set<Long> selected) {
        this(context, selection, null, selected);
    }

    public MediaListAdapter(Context context, String selection, String[] selectionArgs, Set<Long> selected) {
        super(context, AppSqlHelper.instance(context).getReadableDatabase()
                        .query(Media.TABLE, new String[]{"*"}, selection, selectionArgs, null, null, Media.Columns.NAME)
                , false);
        this.mSelection = selected;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.media_item, null);
        view.setOnClickListener(this);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        MediaItem mediaItem = new MediaItem(cursor.getLong(0), cursor.getLong(4), cursor.getInt(10));

        ImageView albumArt = (ImageView) view.findViewById(R.id.albumArt);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView albumNameView = (TextView) view.findViewById(R.id.album);
        TextView artistView = (TextView) view.findViewById(R.id.artist);

        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(mSelection.contains(mediaItem.getId()));
        titleView.setText(cursor.getString(2));
        albumNameView.setText(cursor.getString(5));
        artistView.setText(cursor.getString(6));

        view.setTag(R.id.mediaItem, mediaItem);

        albumArt.setTag(R.id.mediaItem, mediaItem);
        ((ImageCacheActivity) context).loadBitmap(Utils.uriForMediaItem(mediaItem), albumArt, (int) Utils.pxFromDp(view.getContext(), 50));

        albumArt.setOnTouchListener((View.OnTouchListener) context);

        checkBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        View rootView = (View) buttonView.getParent().getParent();
        MediaItem mediaItem = (MediaItem) rootView.getTag(R.id.mediaItem);
        if (!isChecked) mSelection.remove(mediaItem.getId());
        else mSelection.add(mediaItem.getId());
    }

    @Override
    public void onClick(View view) {
        MediaPlayBackTask.stop();
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        checkBox.setChecked(!checkBox.isChecked());
    }

    @Override
    public void onDataSetChanged(Collection<? extends Long> objects, int op) {
        notifyDataSetChanged();
    }


    @Override
    public void onDataSetChanged(Long object, int op) {
        notifyDataSetChanged();
    }
}
