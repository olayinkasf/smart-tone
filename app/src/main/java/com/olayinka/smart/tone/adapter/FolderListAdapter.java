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
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;
import com.olayinka.smart.tone.model.OrderedMediaSet;

/**
 * Created by Olayinka on 5/3/2015.
 */
public class FolderListAdapter extends AlbumListAdapter {

    public FolderListAdapter(Context context, OrderedMediaSet<Long> selected) {
        super(context, Media.Folder.TABLE, selected);
    }

    @Override
    String getTable() {
        return Media.Folder.TABLE;
    }

    @Override
    public MediaItem getItemForAlbumArt(Cursor cursor) {
        return new MediaItem(cursor.getLong(0), cursor.getLong(1), cursor.getInt(4));
    }

}
