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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Olayinka on 5/3/2015.
 */
public class MediaItem implements Comparable<MediaItem>, Parcelable {
    long mId;
    long mAlbumId;
    int mInternal;


    protected MediaItem(Parcel in) {
        mId = in.readLong();
        mAlbumId = in.readLong();
        mInternal = in.readInt();
    }

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
        public MediaItem createFromParcel(Parcel in) {
            return new MediaItem(in);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };

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

    public long getId() {
        return mId;
    }

    public long getAlbumId() {
        return mAlbumId;
    }

    public int getInternal() {
        return mInternal;
    }

    @Override
    public int compareTo(MediaItem another) {
        if (mAlbumId < another.mAlbumId) return -1;
        if (mAlbumId > another.mAlbumId) return 1;
        if (mInternal < another.mInternal) return -1;
        if (mInternal > another.mInternal) return 1;
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeLong(mAlbumId);
        dest.writeInt(mInternal);
    }
}
