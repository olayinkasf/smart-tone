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
    private Context mContext;

    public SelectionListAdapter(Context context, Set<Long> selected) {
        super(context, String.format(MediaListAdapter.SELECTION_SELECTED, TextUtils.join(", ", selected)), null, selected);
        mContext = context;
    }

    public void requery() {
        Cursor cursor = AppSqlHelper.instance(mContext).getReadableDatabase()
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

    @Override
    public void onDataSetChanged() {
        requery();
    }
}
