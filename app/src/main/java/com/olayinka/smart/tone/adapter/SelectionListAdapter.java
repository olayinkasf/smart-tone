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
