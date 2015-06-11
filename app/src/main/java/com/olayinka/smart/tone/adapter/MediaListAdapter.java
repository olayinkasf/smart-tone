package com.olayinka.smart.tone.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.activity.ImageCacheActivity;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;
import com.olayinka.smart.tone.task.MediaPlayBackTask;
import lib.olayinka.smart.tone.R;

import java.util.Set;

/**
 * Created by Olayinka on 5/3/2015.
 */

public class MediaListAdapter extends CursorAdapter implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {


    public static final String SELECTION_ALL = null;
    public static final String SELECTION_RINGTONE = Media.Columns.IS_NOTIFICATION + " == 0";
    public static final String SELECTION_NOTIFICATION = Media.Columns.IS_NOTIFICATION + " != 0";
    public static final String SELECTION_SELECTED = Media.Columns._ID + " IN (%s)";
    public static SelectionListAdapter SELECTION_ADAPTER;

    protected Set<Long> mSelection;

    public MediaListAdapter(Context context, String selection, Set<Long> selected) {
        this(context, selection, null, selected);
    }

    public MediaListAdapter(Context context, String selection, String[] selectionArgs, Set<Long> selected) {
        super(context, AppSqlHelper.instance(context).getReadableDatabase()
                .query(Media.TABLE, new String[]{"*"}, selection, null, null, null, Media.Columns.NAME)
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
        ((ImageCacheActivity) albumArt.getContext()).loadBitmap(Utils.uriForMediaItem(mediaItem), albumArt, (int) Utils.pxFromDp(view.getContext(), 50));

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
        if (SELECTION_ADAPTER != null) {
            SELECTION_ADAPTER.requery(view.getContext());
        }
    }
}
