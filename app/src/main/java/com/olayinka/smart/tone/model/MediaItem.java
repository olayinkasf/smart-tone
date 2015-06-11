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
