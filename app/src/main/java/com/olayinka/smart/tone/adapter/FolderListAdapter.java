package com.olayinka.smart.tone.adapter;

import android.content.Context;
import android.database.Cursor;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;

import java.util.Set;

/**
 * Created by Olayinka on 5/3/2015.
 */
public class FolderListAdapter extends AlbumListAdapter {

    public FolderListAdapter(Context context, Set<Long> selected) {
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
