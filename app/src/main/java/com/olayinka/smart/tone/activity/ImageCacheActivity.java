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

package com.olayinka.smart.tone.activity;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.listener.DoubleTapListener;
import com.olayinka.smart.tone.model.MediaItem;
import com.olayinka.smart.tone.task.AsyncTask;
import com.olayinka.smart.tone.task.MediaPlayBackTask;
import lib.olayinka.smart.tone.R;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Olayinka on 5/6/2015.
 */
public abstract class ImageCacheActivity extends AppCompatActivity implements View.OnTouchListener {
    protected Bitmap mPlaceHolderBitmap;
    private LruCache<String, Bitmap> mMemoryCache;
    private GestureDetector mDetector;
    private DoubleTapListener mDoubleTapListener;

    public static boolean cancelPotentialWork(Uri data, ImageView imageView) {
        final UriBitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Uri bitmapData = bitmapWorkerTask.getData();
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || !bitmapData.equals(data)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static UriBitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    public static Bitmap decodeSampledBitmapFromImageUri(ContentResolver resolver, Uri uri, int reqWidth, int reqHeight) {

        AssetFileDescriptor fd = null;
        try {
            fd = resolver.openAssetFileDescriptor(uri, "r");
            if (fd != null) {
                return decodeSampledBitmapFromDescriptor(fd.getFileDescriptor(), reqWidth, reqHeight);
            }
        } catch (SecurityException | IOException ignored) {
        } finally {
            try {
                if (fd != null) {
                    fd.close();
                }
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        initToolbar(toolbar);
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void loadBitmap(Uri uri, ImageView imageView, int size, String keySuffix) {
        Bitmap bitmap = getBitmapFromMemCache(String.valueOf(uri) + keySuffix);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else if (cancelPotentialWork(uri, imageView)) {
            final UriBitmapWorkerTask task = new UriBitmapWorkerTask(imageView, size, keySuffix);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(uri);
        }
    }

    public void loadBitmap(Uri uri, ImageView imageView, int size) {
        loadBitmap(uri, imageView, size, "");
    }

    abstract void initToolbar(Toolbar toolbar);

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        MediaItem mediaItem = (MediaItem) v.getTag(R.id.mediaItem);
        mDoubleTapListener.setMediaItem(mediaItem);
        return mDetector.onTouchEvent(event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMemoryCache.evictAll();
        MediaPlayBackTask.stop();
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<UriBitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, UriBitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<>(bitmapWorkerTask);
        }

        public UriBitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    private class UriBitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        protected int mSize;
        private Uri mData;
        String keySuffix;

        public UriBitmapWorkerTask(ImageView imageView, int size, String keySuffix) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
            this.mSize = size;
            this.keySuffix = keySuffix;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                addBitmapToMemoryCache(String.valueOf(mData) + keySuffix, bitmap);
            }
            if (isCancelled()) {
                bitmap = null;
            }
            //noinspection ConstantConditions
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final UriBitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                //noinspection ConstantConditions
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

        public Uri getData() {
            return mData;
        }


        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Uri... params) {
            mData = params[0];
            return decodeSampledBitmapFromImageUri(getContentResolver(), params[0], mSize, mSize);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDoubleTapListener();
        setMemoryCache();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setActionBar();
    }

    private void setMemoryCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        int size = (int) Utils.pxFromDp(this, 100);
        mPlaceHolderBitmap = decodeSampledBitmapFromResource(getResources(), R.mipmap.album_unknown, size, size);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                if (Utils.hasHoneycombMR1())
                    return bitmap.getByteCount() / 1024;
                return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
            }
        };
    }

    private void setDoubleTapListener() {
        mDoubleTapListener = new DoubleTapListener(this);
        mDetector = new GestureDetector(this, mDoubleTapListener);
    }
}
