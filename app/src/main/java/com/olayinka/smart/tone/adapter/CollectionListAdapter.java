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

import android.animation.*;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.olayinka.smart.tone.*;
import com.olayinka.smart.tone.activity.AbstractMenuActivity;
import com.olayinka.smart.tone.activity.CollectionEditActivity;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;
import com.olayinka.smart.tone.task.AsyncTask;
import com.olayinka.smart.tone.widget.CenterTopImageView;
import lib.olayinka.smart.tone.R;
import org.json.JSONException;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created by Olayinka on 5/3/2015.
 */
public class CollectionListAdapter extends CursorAdapter {

    private HashMap<Long, TreeSet<MediaItem>> mTonesMap;
    private HashMap<Long, ToneLoader> mToneLoaderMap;
    private View.OnClickListener mItemClickListener;
    private Long[] mActivePairs;
    private AbstractMenuActivity mActivity;

    public CollectionListAdapter(Context context) {
        super(context, cursor(context), false);
        mTonesMap = new HashMap<>(50);
        mToneLoaderMap = new HashMap<>(50);
        mItemClickListener = (View.OnClickListener) context;
        mActivity = (AbstractMenuActivity) context;
        mActivePairs = AppSettings.getActivePairs(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.collection_item, null);
        view.setOnClickListener(mItemClickListener);
        RelativeLayout albumTable = (RelativeLayout) view.findViewById(R.id.albumArt);
        for (int i = 0; i < AbstractMenuActivity.NUM_THUMBNAILS - 1; i++)
            albumTable.addView(new CenterTopImageView(context));
        ImageView transitionImageView = new CenterTopImageView(context);
        if (Utils.hasLollipop()) {
            transitionImageView.setId(R.id.transitionImage);
            transitionImageView.setTransitionName(context.getString(R.string.transition_collection_page));
        }
        albumTable.addView(transitionImageView);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                collectionToolbarMenuClicked(view, menuItem);
                return true;
            }
        });

        return view;
    }

    private void collectionToolbarMenuClicked(final View view, MenuItem menuItem) {
        final long collectionId = (long) view.getTag(R.id.collectionId);
        final String collectionName = (String) view.getTag(R.id.collectionName);
        final Context context = view.getContext();
        if (menuItem.getItemId() == R.id.deleteCollection) {
            deleteCollection(collectionId, collectionName, view);
        } else if (menuItem.getItemId() == R.id.editCollection) {
            Intent intent = new Intent(context, CollectionEditActivity.class);
            intent.putExtra(CollectionEditActivity.COLLECTION_ID, collectionId);
            context.startActivity(intent);
        } else if (menuItem.getItemId() == R.id.removeNotif) {
            new AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.remove) + " " + collectionName + " " + context.getString(R.string.as_notif) + "?")
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivePairs[1] = 0l;
                            context.getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE).edit().putLong(AppSettings.ACTIVE_NOTIFICATION, 0l).apply();
                            mActivity.refreshForChange();
                        }
                    }).show();
        } else if (menuItem.getItemId() == R.id.setNotif) {
            new AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.set) + " " + collectionName + " " + context.getString(R.string.as_notif) + "?")
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivePairs[1] = collectionId;
                            SharedPreferences prefs = context.getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE);
                            prefs.edit().putLong(AppSettings.ACTIVE_NOTIFICATION, collectionId).apply();
                            prefs.edit().putBoolean(AppSettings.ACTIVE_APP_SERVICE, true).apply();
                            try {
                                AppSettings.changeNotificationSound(context, true);
                                ((AbsSmartTone) context.getApplicationContext()).startServices();
                                mActivity.refreshForChange();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).show();
        } else if (menuItem.getItemId() == R.id.removeRingtone) {
            new AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.remove) + " " + collectionName + " " + context.getString(R.string.as_ringtone) + "?")
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivePairs[0] = 0l;
                            context.getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE).edit().putLong(AppSettings.ACTIVE_RINGTONE, 0l).apply();
                            mActivity.refreshForChange();
                        }
                    }).show();
        } else if (menuItem.getItemId() == R.id.setRingtone) {
            new AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.set) + " " + collectionName + " " + context.getString(R.string.as_ringtone) + "?")
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivePairs[0] = collectionId;
                            SharedPreferences prefs = context.getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE);
                            prefs.edit().putLong(AppSettings.ACTIVE_RINGTONE, collectionId).apply();
                            prefs.edit().putBoolean(AppSettings.ACTIVE_APP_SERVICE, true).apply();

                            try {
                                AppSettings.changeRingtone(context, true);
                                ((AbsSmartTone) context.getApplicationContext()).startServices();
                                mActivity.refreshForChange();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }).show();
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        view.setTag(R.id.collectionId, cursor.getLong(0));
        view.setTag(R.id.collectionName, cursor.getString(1));

        RelativeLayout albumTable = (RelativeLayout) view.findViewById(R.id.albumArt);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(cursor.getString(1));
        toolbar.getMenu().clear();

        if (cursor.getLong(0) == mActivePairs[0] && cursor.getLong(0) == mActivePairs[1])
            toolbar.inflateMenu(R.menu.collection_both);
        else if (cursor.getLong(0) == mActivePairs[0])
            toolbar.inflateMenu(R.menu.collection_ringtone);
        else if (cursor.getLong(0) == mActivePairs[1])
            toolbar.inflateMenu(R.menu.collection_notif);
        else
            toolbar.inflateMenu(R.menu.collection);

        long id = cursor.getLong(0);
        if (mTonesMap.get(id) == null && mToneLoaderMap.get(id) == null) {
            ToneLoader toneLoader = new ToneLoader(id);
            mToneLoaderMap.put(id, toneLoader);
            toneLoader.execute(view.getContext());
        }
        ((AbstractMenuActivity) albumTable.getContext()).loadBitmap(albumTable, id, mTonesMap);
    }

    public TreeSet<MediaItem> getItemsForCollection(long tag) {
        return mTonesMap.get(tag);
    }

    public void changeCursor(Context context) {
        changeCursor(cursor(context));
        notifyDataSetChanged();
    }

    private void deleteCollection(final long collectionId, final String name, final View view) {
        final Context context = (Context) mItemClickListener;
        new AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.delete) + " " + name + "?")
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppSettings.deleteCheck(context, collectionId);
                        Media.deleteCollection(context, collectionId);
                        Utils.toast(context, context.getString(R.string.collection) + " " + name + " " + context.getString(R.string.deleted));
                        gotIt(context, view);
                    }
                }).show();
    }

    private void gotIt(final Context context, final View header) {
        if (header.getVisibility() == View.GONE)
            return;
        final int height = header.getMeasuredHeight();
        final AbsListView.LayoutParams layoutParams = (AbsListView.LayoutParams) header.getLayoutParams();

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(context, "alpha", 0f).setDuration(300);

        ValueAnimator heightAnimator = ValueAnimator.ofInt(height, 0).setDuration(400);
        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                layoutParams.height = (Integer) valueAnimator.getAnimatedValue();
                header.setLayoutParams(layoutParams);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(alphaAnimator, heightAnimator);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                header.setVisibility(View.GONE);
                mActivity.refreshForChange();
            }
        });

        set.start();
    }

    class ToneLoader extends AsyncTask<Object, Void, TreeSet<MediaItem>> {

        long mId;

        public ToneLoader(long mId) {
            this.mId = mId;
        }

        @Override
        protected void onPostExecute(TreeSet<MediaItem> o) {
            mTonesMap.put(mId, o);
            mToneLoaderMap.remove(mId);
            notifyDataSetChanged();
        }

        @Override
        protected TreeSet<MediaItem> doInBackground(Object... params) {
            AppSqlHelper sqlHelper = AppSqlHelper.instance((Context) params[0]);
            SQLiteDatabase database = sqlHelper.getReadableDatabase();
            Cursor cursor = database.rawQuery(
                    "select m.media_id, m.album_id, m.is_internal\n" +
                            "from media m inner join tone t\n" +
                            "on m._id = t.media_id\n" +
                            "where t.collection_id = ?",
                    new String[]{"" + mId}
            );
            TreeSet<MediaItem> items = new TreeSet<>();
            while (cursor.moveToNext() && items.size() < AbstractMenuActivity.NUM_THUMBNAILS) {

                MediaItem mediaItem = new MediaItem(cursor.getLong(0), cursor.getLong(1), cursor.getInt(2));
                if (Utils.isValidUri((Context) params[0], Utils.uriForMediaItem(mediaItem))) {
                    items.add(mediaItem);
                }
            }
            cursor.close();
            return items;
        }
    }

    public static MergeCursor cursor(Context context) {
        Long[] activePairs = AppSettings.getActivePairs(context);
        Cursor ringtoneCursor = AppSqlHelper.instance(context).getReadableDatabase()
                .query(Media.Collection.TABLE, new String[]{"*"}, Media.CollectionColumns._ID + Media.EQUALS, new String[]{"" + activePairs[0]}, null, null, null);
        Cursor notifCursor = null;
        if (!activePairs[0].equals(activePairs[1]))
            notifCursor = AppSqlHelper.instance(context).getReadableDatabase()
                    .query(Media.Collection.TABLE, new String[]{"*"}, Media.CollectionColumns._ID + Media.EQUALS, new String[]{"" + activePairs[1]}, null, null, null);
        Cursor othersCursor = AppSqlHelper.instance(context).getReadableDatabase()
                .query(Media.Collection.TABLE, new String[]{"*"}, Media.CollectionColumns._ID + " NOT IN (" + TextUtils.join(", ", activePairs) + ")", null, null, null, Media.CollectionColumns.NAME);

        if (notifCursor != null)
            return new MergeCursor(new Cursor[]{ringtoneCursor, notifCursor, othersCursor});
        else
            return new MergeCursor(new Cursor[]{ringtoneCursor, othersCursor});
    }
}
