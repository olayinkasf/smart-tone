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

package com.olayinka.smart.tone.listener;

import android.content.Context;
import android.database.Cursor;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;
import com.olayinka.smart.tone.task.MediaPlayBackTask;
import lib.olayinka.smart.tone.R;

import java.io.IOException;

/**
 * Created by Olayinka on 12/26/2014.
 */
public class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {

    private Context mContext;
    private MediaItem mediaItem;

    public DoubleTapListener(Context mContext) {
        this.mContext = mContext;
    }

    public boolean onDoubleTap(MotionEvent motionEvent) {
        Cursor cursor = null;
        try {
            cursor = AppSqlHelper.instance(mContext).getReadableDatabase()
                    .query(
                            Media.TABLE, new String[]{"*"},
                            Media.Columns._ID + Media.EQUALS + Media.AND + Media.Columns.IS_INTERNAL + Media.EQUALS,
                            new String[]{"" + mediaItem.getId(), "" + mediaItem.getInternal()}, null, null, null
                    );
            cursor.moveToNext();
            MediaPlayBackTask.play(mContext, cursor.getString(3));
        } catch (IOException localIOException) {
            cursor.close();
            localIOException.printStackTrace();
            return true;
        } catch (Exception ignored) {
            return true;
        }
        Utils.toast(mContext, mContext.getString(R.string.previewing) + " " + cursor.getString(2));
        return true;
    }

    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        MediaPlayBackTask.stop();
        return false;
    }

    public void setMediaItem(MediaItem mediaItem) {
        this.mediaItem = mediaItem;
    }
}