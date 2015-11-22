
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

import android.animation.*;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.*;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.*;
import com.melnykov.fab.FloatingActionButton;
import com.olayinka.rate.app.RateThisAppAlert;
import com.olayinka.smart.tone.*;
import com.olayinka.smart.tone.adapter.CollectionListAdapter;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;
import com.olayinka.smart.tone.service.IndexerService;
import com.olayinka.smart.tone.widget.PrefsSwitchCompat;
import lib.olayinka.smart.tone.BuildConfig;
import lib.olayinka.smart.tone.R;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by Olayinka on 7/8/2015.
 */
public abstract class AbstractMenuActivity extends ImageCacheActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String CONCRETE = "com.olayinka.smart.tone.activity.MenuActivity";
    public static final String CHANGE_RINGTONE = "change.ringtone";
    public static final String CHANGE_NOTIFICATION_SOUND = "change.notification.sound";
    public static final String CHANGE_RINGTONE_SERVICE = "change.ringtone.service";
    public static final String CHANGE_NOTIFICATION_SOUND_SERVICE = "change.notification.sound.service";
    public static int NUM_THUMBNAILS = 6;
    private ListView mListView;
    private View mListHeader;
    private CollectionListAdapter mAdapter;
    private int mAlbumArtWidth;
    private DisplayMetrics mDisplayDimens;
    private boolean mOnStartFlag = false;

    public Intent mPendingIntent = null;

    BroadcastReceiver mShuffleObserver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra(AppSettings.JUST_CHANGED)) {
                case AppSettings.ACTIVE_NOTIFICATION:
                    refreshCurrentNotif(false);
                    return;
                case AppSettings.ACTIVE_RINGTONE:
                    refreshCurrentRingTone(false);
                    break;
            }

        }
    };

    @Override
    void initToolbar(Toolbar toolbar) {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    protected void getVersion() {
        Map<String, String> varMap = Utils.VAR_MAP;
        varMap.put("build.time", Long.toHexString(BuildConfig.BUILD_TIME));
        varMap.put("build.number", String.valueOf(BuildConfig.BUILD_NUMBER));
        getVersion(varMap);
        if ("debug".equalsIgnoreCase(varMap.get("build.type"))) {
            // getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE)
            //    .edit().clear().commit();
            //getSharedPreferences(RateThisAppAlert.ID, MODE_PRIVATE)
            //      .edit().clear().commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        getVersion();
        mDisplayDimens = Utils.displayDimens(this);
        mAlbumArtWidth = (int) ((mDisplayDimens.widthPixels - Utils.pxFromDp(this, 20.0f)) / 2);
        mListView = (ListView) findViewById(R.id.list);
        Log.wtf("dfgh", "" + R.id.list);
        Log.wtf("dfgh", "" + mListView);
        mListHeader = LayoutInflater.from(this).inflate(R.layout.menu_header, null);
        mListView.addHeaderView(mListHeader);
        mListView.setEmptyView(findViewById(R.id.empty));
        mAdapter = new CollectionListAdapter(this);
        mListView.setAdapter(mAdapter);
        View footerView = new View(this);
        mListView.addFooterView(footerView);
        footerView.setLayoutParams(new ViewGroup.LayoutParams(mDisplayDimens.widthPixels / 4, mDisplayDimens.widthPixels / 4));
        setCreateButtonClickListener();

        setServiceSwitchButton();
        refreshCurrentRingTone(true);
        refreshCurrentNotif(true);
        refreshServiceNotifier(true);
        refreshSelectNotifier(true);

        mOnStartFlag = false;

        if (showAskAppLog() || showRateThisApp()) ;
    }

    private boolean showAskAppLog() {

        if (getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE).getBoolean(AppSettings.ASK_LOG_APP_ACTIVITY, false))
            return false;
        getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE).edit().putBoolean(AppSettings.ASK_LOG_APP_ACTIVITY, true).apply();

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("What is new")
                .setMessage("To maintain functionalities across multiple devices, the application logs its activities and save them in a file on the device. We cannot retrieve these log files without your permission so if you come across a bug that you'd like to report please use the contact option in the drop down menu and attach log files with it. \n\nTo enable logging, please click Yes below, otherwise click No.")
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE).edit().putBoolean(AppSettings.LOG_APP_ACTIVITY, false).apply();
                    }
                })
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE).edit().putBoolean(AppSettings.LOG_APP_ACTIVITY, true).apply();
                    }
                })
                .show();
        return true;
    }

    private boolean showRateThisApp() {
        return new RateThisAppAlert(this)
                .remind(true, 7)
                .addPrefsCondition(AppSettings.APP_SETTINGS, AppSettings.RINGTONE_FREQ + AppSettings.LAST_CHANGE, 0L, RateThisAppAlert.GREATER)
                .addPrefsCondition(AppSettings.APP_SETTINGS, AppSettings.ACTIVE_APP_SERVICE)
                .minLaunchWait(5)
                .show().isShowing();
    }

    private void refreshServiceNotifier(boolean init) {
        Long[] activePairs = AppSettings.getActivePairs(this);
        boolean active = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE).getBoolean(AppSettings.ACTIVE_APP_SERVICE, false);
        if ((activePairs[0] == 0 && activePairs[1] == 0) || active) {
            hide(mListHeader.findViewById(R.id.serviceNotifier), init);
        } else {
            show(mListHeader.findViewById(R.id.serviceNotifier), init, null);
        }
    }

    private void show(View view, boolean init, Float v) {
        if (init)
            view.setVisibility(View.VISIBLE);
        else
            unGotIt(view, v);
    }

    private void hide(View view, boolean init) {
        if (!init)
            gotIt(view);
        else {
            view.setVisibility(View.GONE);
            view.getLayoutParams().height = 0;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mOnStartFlag)
            refresh();
        mOnStartFlag = true;
        registerReceiver(mShuffleObserver, new IntentFilter(AppSettings.JUST_CHANGED));
    }

    public void refresh() {
        setServiceSwitchButton();
        refreshCurrentRingTone(false);
        refreshCurrentNotif(false);
        refreshServiceNotifier(false);
        refreshSelectNotifier(false);
        mAdapter.changeCursor(this);
    }

    public void refreshForChange() {
        setServiceSwitchButton();
        refreshServiceNotifier(false);
        refreshSelectNotifier(false);
        mAdapter.changeCursor(this);
    }

    private void refreshSelectNotifier(boolean init) {
        Long[] activePairs = AppSettings.getActivePairs(this);
        if (activePairs[0] == 0l && activePairs[1] == 0l) {
            show(findViewById(R.id.selectNotifier), init, null);
        } else {
            hide(findViewById(R.id.selectNotifier), init);
        }
    }

    private void setCreateButtonClickListener() {
        findViewById(R.id.createCollection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AbstractMenuActivity.this, CollectionEditActivity.class));
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.createCollection);
        fab.attachToListView(mListView);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionsActivity.PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                switch (mPendingIntent.getAction()) {
                    case CHANGE_NOTIFICATION_SOUND:
                        AppSettings.changeNotificationSound(this, true);
                        break;
                    case CHANGE_RINGTONE:
                        AppSettings.changeRingtone(this, true);
                        break;
                    case CHANGE_NOTIFICATION_SOUND_SERVICE:
                        AppSettings.changeNotificationSound(this, true);
                        ((AbsSmartTone) getApplication()).startServices();
                        break;
                    case CHANGE_RINGTONE_SERVICE:
                        AppSettings.changeRingtone(this, true);
                        ((AbsSmartTone) getApplication()).startServices();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked)
            ((AbsSmartTone) getApplication()).startServices();
        else ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        refreshServiceNotifier(false);
    }

    private void setServiceSwitchButton() {
        SharedPreferences prefs = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        PrefsSwitchCompat serviceSwitch = (PrefsSwitchCompat) findViewById(R.id.serviceSwitch);
        boolean enabled = (prefs.getLong(AppSettings.ACTIVE_NOTIFICATION, 0) != 0
                || prefs.getLong(AppSettings.ACTIVE_RINGTONE, 0) != 0)
                && AppSqlHelper.hasData(this, Media.Collection.TABLE);
        serviceSwitch.setEnabled(enabled);
        serviceSwitch.setOnCheckedChangeListener(null);
        serviceSwitch.setChecked(enabled & prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false));
        serviceSwitch.setOnCheckedChangeListener(this);
    }

    private void refreshCurrentNotif(boolean init) {
        Uri uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
        refresh(uri, mListHeader.findViewById(R.id.currentNotif), init);
        if (init) {
            mListHeader.findViewById(R.id.currentNotif).findViewById(R.id.toggleTone).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences prefs = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);

                    if (prefs.getLong(AppSettings.ACTIVE_NOTIFICATION, 0) == 0) {
                        Utils.toast(v.getContext(), getString(R.string.no_active_notification));
                        return;
                    }
                    if (!prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false)) {
                        Utils.toast(v.getContext(), getString(R.string.service_inactive));
                        return;
                    }
                    try {
                        AppSettings.changeNotificationSound(v.getContext(), true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utils.toast(v.getContext(), getString(R.string.shuffle_error));
                    }
                }
            });
        }
    }

    private void refreshCurrentRingTone(boolean init) {
        Uri uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
        refresh(uri, mListHeader.findViewById(R.id.currentRingtone), init);
        if (init) {
            mListHeader.findViewById(R.id.currentRingtone).findViewById(R.id.toggleTone).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences prefs = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);

                    if (prefs.getLong(AppSettings.ACTIVE_RINGTONE, 0) == 0) {
                        Utils.toast(v.getContext(), getString(R.string.no_active_ringtone));
                        return;
                    }
                    if (!prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false)) {
                        Utils.toast(v.getContext(), getString(R.string.service_inactive));
                        return;
                    }
                    try {
                        AppSettings.changeRingtone(v.getContext(), true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utils.toast(v.getContext(), getString(R.string.shuffle_error));
                    }
                }
            });
        }
    }

    private void refresh(Uri uri, View view, boolean init) {
        if (uri == null) {
            hide(view.findViewById(R.id.properties), init);
            return;
        }
        Cursor cursor = getContentResolver().query(uri, IndexerService.PROJECTION, null, null, null);

        if (cursor == null) {
            hide(view.findViewById(R.id.properties), init);
            return;
        }
        if (!cursor.moveToNext()) {
            cursor.close();
            hide(view.findViewById(R.id.properties), init);
            return;
        }

        long id = ContentUris.parseId(uri);
        boolean internal = uri.toString().contains("internal");
        MediaItem mediaItem = new MediaItem(id, cursor.getLong(3), internal ? 1 : 0);

        ImageView albumArt = (ImageView) view.findViewById(R.id.albumArt);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView albumNameView = (TextView) view.findViewById(R.id.album);
        TextView artistView = (TextView) view.findViewById(R.id.artist);

        titleView.setText(cursor.getString(1));
        albumNameView.setText(cursor.getString(4));
        artistView.setText(cursor.getString(5));

        cursor.close();
        cursor = AppSqlHelper.instance(this).getReadableDatabase()
                .query(
                        Media.TABLE, new String[]{Media.Columns._ID},
                        Media.Columns.MEDIA_ID + Media.EQUALS + Media.AND + Media.Columns.IS_INTERNAL + Media.EQUALS,
                        new String[]{"" + mediaItem.getId(), "" + mediaItem.getInternal()}, null, null, null
                );
        if (cursor != null && cursor.moveToNext()) {
            mediaItem.setId(cursor.getLong(0));
            cursor.close();
        }

        albumArt.setClickable(true);
        albumArt.setOnTouchListener(this);
        view.setTag(R.id.mediaItem, mediaItem);
        albumArt.setTag(R.id.mediaItem, mediaItem);


        loadBitmap(Utils.uriForMediaItem(mediaItem), albumArt, (int) Utils.pxFromDp(view.getContext(), 50));
        show(view.findViewById(R.id.properties), init, Utils.pxFromDp(this, 100.0f));
    }

    public void loadBitmap(RelativeLayout albumArts, long collectionId, HashMap<Long, TreeSet<MediaItem>> itemsMap) {
        TreeSet<MediaItem> items = itemsMap.get(collectionId);
        albumArts.getLayoutParams().height = mAlbumArtWidth;
        albumArts.getLayoutParams().width = mAlbumArtWidth * 2;

        //default
        for (int i = 0; i < NUM_THUMBNAILS - 1; i++) albumArts.getChildAt(i).setVisibility(View.GONE);
        ImageView imageView = (ImageView) albumArts.getChildAt(NUM_THUMBNAILS - 1);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(params);
        imageView.setImageBitmap(mPlaceHolderBitmap);

        if (items == null || items.size() == 0) return;

        switch (items.size()) {
            case 1: {
                MediaItem mediaItem = items.last();
                imageView = (ImageView) albumArts.getChildAt(NUM_THUMBNAILS - 1);
                loadBitmap(Utils.uriForMediaItem(mediaItem), imageView, mAlbumArtWidth);
            }
            break;
            default: {
                for (int i = 0; i < NUM_THUMBNAILS - items.size(); i++)
                    albumArts.getChildAt(i).setVisibility(View.GONE);
                for (int i = NUM_THUMBNAILS - items.size(); i < NUM_THUMBNAILS; i++)
                    albumArts.getChildAt(i).setVisibility(View.VISIBLE);
                int i = 0;
                for (MediaItem mediaItem : items) {
                    int pos = NUM_THUMBNAILS - items.size() + i;
                    imageView = (ImageView) albumArts.getChildAt(pos);
                    params = new RelativeLayout.LayoutParams(mAlbumArtWidth, mAlbumArtWidth);
                    params.setMargins(mAlbumArtWidth - (i * mAlbumArtWidth) / (items.size() - 1), 0, 0, 0);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    loadBitmap(Utils.uriForMediaItem(mediaItem), imageView, mAlbumArtWidth);
                    i++;
                }
            }
            break;
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View v) {
        MediaItem mediaItem = null;
        long collectionId = (long) v.getTag(R.id.collectionId);
        TreeSet<MediaItem> items = mAdapter.getItemsForCollection((long) v.getTag(R.id.collectionId));
        if (items != null && items.size() > 0) {
            mediaItem = items.last();
        }
        ImageView imageView = (ImageView) v.findViewById(R.id.transitionImage);
        Intent intent = new Intent(this, CollectionPageActivity.class);
        intent.putExtra(CollectionPageActivity.ART_MEDIA_ITEM, mediaItem == null ? null : mediaItem.toString());
        intent.putExtra(CollectionPageActivity.COLLECTION_ID, collectionId);
        if (Utils.hasLollipop()) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageView, getString(R.string.transition_collection_page));
            ((Activity) v.getContext()).startActivityForResult(intent, CollectionPageActivity.GROUP_RETURN_CODE, options.toBundle());
        } else ((Activity) v.getContext()).startActivityForResult(intent, CollectionPageActivity.GROUP_RETURN_CODE);
        //finish();
    }

    private void gotIt(final View header) {
        if (header.getVisibility() == View.GONE)
            return;
        final int height = header.getMeasuredHeight();
        final ViewGroup.LayoutParams layoutParams = header.getLayoutParams();

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f).setDuration(300);

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
            }
        });

        set.start();
    }

    private void unGotIt(final View header, Float v) {
        if (header.getVisibility() == View.VISIBLE)
            return;
        header.measure(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT);
        int height = header.getMeasuredHeight();
        if (v != null)
            height = (int) (float) v;
        final ViewGroup.LayoutParams layoutParams = header.getLayoutParams();
        header.setVisibility(View.VISIBLE);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 1.0f).setDuration(300);

        ValueAnimator heightAnimator = ValueAnimator.ofInt(layoutParams.height, height).setDuration(400);
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
                header.setVisibility(View.VISIBLE);
            }
        });

        set.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mShuffleObserver);
    }

    protected abstract void getVersion(Map<String, String> varMap);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (item.getItemId() == R.id.aboutApp) {
            startActivity(new Intent(this, AboutAppActivity.class));
        } else if (item.getItemId() == R.id.contactHelp) {
            startActivity(new Intent(this, ContactActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
