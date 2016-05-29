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
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import com.mobeta.android.dslv.DragSortCursorAdapter;
import com.olayinka.smart.tone.AppLogger;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.activity.ImageCacheActivity;
import com.olayinka.smart.tone.model.ListenableHashSet;
import com.olayinka.smart.tone.model.MediaItem;
import com.olayinka.smart.tone.model.OrderedMediaSet;
import com.olayinka.smart.tone.model.SelectionCursor;
import lib.olayinka.smart.tone.R;

import java.util.Collection;

/**
 * Created by Olayinka on 5/24/2015.
 */
public class SelectionListAdapter extends DragSortCursorAdapter implements ListenableHashSet.HashSetListener<Long> {

    private Context mContext;

    public SelectionListAdapter(Context context, OrderedMediaSet<Long> selected) {
        super(context, new SelectionCursor(context, selected), false);
        this.mSelection = selected;
        this.mContext = context;
    }

    public void requery() {
        swapCursor(new SelectionCursor(mContext, mSelection));
        notifyDataSetChanged();
    }


    protected OrderedMediaSet<Long> mSelection;


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.drag_media_item, null);
        view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
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

        albumArt.setTag(R.id.mediaItem, mediaItem);
        ((ImageCacheActivity) context).loadBitmap(Utils.uriForMediaItem(mediaItem), albumArt, (int) Utils.pxFromDp(view.getContext(), 50));

        albumArt.setOnTouchListener((View.OnTouchListener) context);
    }

    @Override
    public void drop(int from, int to) {
        super.drop(from, to);
        AppLogger.wtf(mContext, "drop", "from: " + from + ", to: " + to);
        mSelection.changePosition(from, to);
    }

    @Override
    public void remove(int which) {
        super.remove(which);
        AppLogger.wtf(mContext, "removed", "which: " + which);
        mSelection.remove(which);
    }

    @Override
    public void onDataSetChanged(Collection<? extends Long> objects, int op) {
        requery();
    }

    @Override
    public void onDataSetChanged(Long object, int op) {
        requery();
    }
}

