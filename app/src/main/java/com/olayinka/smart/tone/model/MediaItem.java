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

package com.olayinka.smart.tone.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Olayinka on 5/3/2015.
 */
public class MediaItem implements Comparable<MediaItem> {
    long mId;
    long mAlbumId;
    int mInternal;


    public void setId(long mId) {
        this.mId = mId;
    }

    private MediaItem() {

    }

    public MediaItem(long id, long albumId, int internal) {
        this.mId = id;
        this.mAlbumId = albumId;
        this.mInternal = internal;
    }

    public static MediaItem fromJSONObject(JSONObject jsonObject) throws JSONException {
        MediaItem mediaItem = new MediaItem();
        mediaItem.mId = jsonObject.getLong(Media.Columns._ID);
        mediaItem.mAlbumId = jsonObject.getLong(Media.Columns.ALBUM_ID);
        mediaItem.mInternal = jsonObject.getInt(Media.Columns.IS_INTERNAL);
        return mediaItem;
    }

    public long getId() {
        return mId;
    }

    public long getAlbumId() {
        return mAlbumId;
    }

    public int getInternal() {
        return mInternal;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Media.Columns._ID, mId);
        jsonObject.put(Media.Columns.ALBUM_ID, mAlbumId);
        jsonObject.put(Media.Columns.IS_INTERNAL, mInternal);
        return jsonObject;
    }

    @Override
    public String toString() {
        try {
            return toJSONObject().toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int compareTo(MediaItem another) {
        if (mAlbumId < another.mAlbumId) return -1;
        if (mAlbumId > another.mAlbumId) return 1;
        if (mInternal < another.mInternal) return -1;
        if (mInternal > another.mInternal) return 1;
        return 0;
    }
}
