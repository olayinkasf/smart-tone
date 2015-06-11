package com.olayinka.smart.tone.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.model.Media;

import java.util.Set;

/**
 * Created by Olayinka on 5/24/2015.
 */
public class SelectionListAdapter extends MediaListAdapter {
    public SelectionListAdapter(Context context,  Set<Long> selected) {
        super(context, String.format(MediaListAdapter.SELECTION_SELECTED, TextUtils.join(", ", selected)), null, selected);
        MediaListAdapter.SELECTION_ADAPTER = this;
    }

    public void requery(Context context) {
        Cursor cursor = AppSqlHelper.instance(context).getReadableDatabase()
                .query(
                        Media.TABLE,
                        new String[]{"*"},
                        String.format(MediaListAdapter.SELECTION_SELECTED, TextUtils.join(", ", mSelection)),
                        null, null, null,
                        Media.Columns.NAME
                );
        swapCursor(cursor);
        notifyDataSetChanged();
    }
}
