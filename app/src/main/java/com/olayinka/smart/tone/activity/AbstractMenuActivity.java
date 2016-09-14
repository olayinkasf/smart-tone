
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
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.olayinka.rate.app.RateThisAppAlert;
import com.olayinka.smart.tone.AppLogger;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.adapter.CollectionListAdapter;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;
import com.olayinka.smart.tone.service.AppService;
import com.olayinka.smart.tone.service.IndexerService;
import com.olayinka.smart.tone.service.SmartToneService;
import com.olayinka.smart.tone.widget.PrefsSwitchCompat;
import com.wangjie.androidbucket.utils.imageprocess.ABShape;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import lib.olayinka.smart.tone.BuildConfig;
import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 7/8/2015.
 */
public abstract class AbstractMenuActivity extends ImageCacheActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener {

    public static final String CONCRETE = "com.olayinka.smart.tone.activity.MenuActivity";
    private static final int FILE_PICKER_CODE = 0x9a4d;
    public static int NUM_THUMBNAILS = 6;
    private ListView mListView;
    private View mListHeader;
    private CollectionListAdapter mAdapter;
    private int mAlbumArtWidth;

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
    private RapidFloatingActionHelper mRapidFloatingActionHelper;


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
        DisplayMetrics displayMetrics = Utils.displayDimens(this);
        mAlbumArtWidth = (int) ((displayMetrics.widthPixels - Utils.pxFromDp(this, 16.0f)) / 2);
        mListView = (ListView) findViewById(R.id.list);
        mListHeader = LayoutInflater.from(this).inflate(R.layout.menu_header, mListView, false);
        mListView.addHeaderView(mListHeader);
        mListView.setEmptyView(findViewById(R.id.empty));
        refreshWhatIsNew(findViewById(R.id.empty).findViewById(R.id.whatIsNew), true);
        mAdapter = new CollectionListAdapter(this);
        mListView.setAdapter(mAdapter);
        View footerView = new View(this);
        footerView.setLayoutParams(new AbsListView.LayoutParams(displayMetrics.widthPixels / 4, displayMetrics.widthPixels / 4));
        mListView.addFooterView(footerView);
        setCreateButtonClickListener();

        setServiceSwitchButton();
        refreshCurrentRingTone(true);
        refreshCurrentNotif(true);
        refreshServiceNotifier(true);
        refreshSelectNotifier(true);
        refreshHelpPage(true);
        refreshWhatIsNew(mListHeader.findViewById(R.id.whatIsNew), true);

        if (showAskAppLog() || showRateThisApp()) ;
    }

    private void refreshHelpPage(boolean init) {
        final View view = mListHeader.findViewById(R.id.helpPage);
        if (view == null) return;
        int dismissed = getSharedPreferences().getInt(AppSettings.GOT_IT_HELP_PAGE, 0);
        if (dismissed >= 3) hide(view, true, true);
        else if (!checked()) hide(view, init, false);
        else {
            if (checked()) show(view, false);
            view.findViewById(R.id.positive).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://smarttone.olayinkasf.com/help-faq"));
                    startActivity(browserIntent);
                }
            });
            view.findViewById(R.id.negative).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPreferences = getSharedPreferences();
                    sharedPreferences.edit().putInt(AppSettings.GOT_IT_HELP_PAGE, sharedPreferences.getInt(AppSettings.GOT_IT_HELP_PAGE, 0) + 1).apply();
                    hide(view, false, true);
                }
            });
        }
    }

    private void refreshCheckSamsung(boolean init) {
        final View view = mListHeader.findViewById(R.id.checkSamsung);
        if (view == null) return;
        if (!android.os.Build.MANUFACTURER.toLowerCase().contains("samsung"))
            hide(view, true, true);
        else if (getSharedPreferences().getBoolean(AppSettings.GOT_IT_SAMSUNG, false))
            hide(view, true, true);
        else if (!checked()) hide(view, init, false);
        else {
            if (checked()) show(view, false);
            view.findViewById(R.id.positive).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSharedPreferences().edit()
                            .putBoolean(AppSettings.GOT_IT_SAMSUNG, true).apply();
                    hide(view, false, true);
                }
            });
        }
    }

    private void refreshWhatIsNew(final View view, boolean init) {
        if (view == null) return;
        if (getSharedPreferences()
                .getBoolean(AppSettings.GOT_IT_WHAT_IS_NEW, false)) {
            hide(view, init, true);
        } else {
            view.findViewById(R.id.positive).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSharedPreferences().edit()
                            .putBoolean(AppSettings.GOT_IT_WHAT_IS_NEW, true).apply();
                    hide(view, false, true);
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refreshWhatIsNew(mListHeader.findViewById(R.id.whatIsNew), false);
                            refreshWhatIsNew(findViewById(R.id.empty).findViewById(R.id.whatIsNew), false);
                        }
                    }, 1000);
                }
            });
        }
    }

    private boolean showAskAppLog() {

        if (getSharedPreferences().getBoolean(AppSettings.ASK_LOG_APP_ACTIVITY, false))
            return false;
        getSharedPreferences().edit().putBoolean(AppSettings.ASK_LOG_APP_ACTIVITY, true).apply();

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("What is new")
                .setMessage("To maintain functionalities across multiple devices, the application logs its activities and save them in a file on the device. We cannot retrieve these log files without your permission so if you come across a bug that you'd like to report please use the contact option in the drop down menu and attach log files with it. \n\nTo enable logging, please click Yes below, otherwise click No.")
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getSharedPreferences().edit().putBoolean(AppSettings.LOG_APP_ACTIVITY, false).apply();
                    }
                })
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getSharedPreferences().edit().putBoolean(AppSettings.LOG_APP_ACTIVITY, true).apply();
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
            hide(mListHeader.findViewById(R.id.serviceNotifier), init, false);
        } else {
            show(mListHeader.findViewById(R.id.serviceNotifier), init);
        }
        refreshCheckSamsung(init);
    }

    private void show(View view, boolean init) {
        if (view.getVisibility() == View.VISIBLE)
            return;
        if (init)
            view.setVisibility(View.VISIBLE);
        else
            expand(view);
    }

    private void hide(View view, boolean init, boolean remove) {
        if (!init) collapse(view, remove);
        else {
            view.setVisibility(View.GONE);
            view.getLayoutParams().height = 0;
            if (remove) ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh();
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
            show(findViewById(R.id.selectNotifier), init);
        } else {
            hide(findViewById(R.id.selectNotifier), init, false);
        }
    }

    private void setCreateButtonClickListener() {

        RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(this);
        rfaContent.setOnRapidFloatingActionContentLabelListListener(this);
        List<RFACLabelItem> items = new ArrayList<>();
        items.add(
                new RFACLabelItem<Integer>().setLabel("Sync. a folder")
                        .setResId(R.drawable.ic_folder_open_white_24dp)
                        .setIconNormalColor(0xff4e342e)
                        .setIconPressedColor(0xff3e2723)
                        .setLabelColor(Color.WHITE)
                        .setLabelSizeSp(14)
                        .setLabelBackgroundDrawable(ABShape.generateCornerShapeDrawable(0xaa000000, (int) Utils.pxFromDp(this, 5)))
                        .setWrapper(1)
        );
        items.add(
                new RFACLabelItem<Integer>().setLabel("Create collection")
                        .setResId(R.mipmap.ic_action_create)
                        .setIconNormalColor(0xff056f00)
                        .setIconPressedColor(0xff0d5302)
                        .setLabelColor(0xff056f00)
                        .setWrapper(2)
        );
        rfaContent.setItems(items)
                .setIconShadowRadius((int) Utils.pxFromDp(this, 5))
                .setIconShadowColor(0xff888888)
                .setIconShadowDy((int) Utils.pxFromDp(this, 5));
        RapidFloatingActionLayout rfaLayout = (RapidFloatingActionLayout) findViewById(R.id.activity_main_rfal);
        RapidFloatingActionButton rfaBtn = (RapidFloatingActionButton) rfaLayout.findViewById(R.id.activity_main_rfab);
        mRapidFloatingActionHelper = new RapidFloatingActionHelper(
                this,
                rfaLayout,
                rfaBtn,
                rfaContent
        ).build();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            SmartToneService.startServicesCheckChanged(this);
        }
        else ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        refreshServiceNotifier(false);
    }

    private void setServiceSwitchButton() {
        PrefsSwitchCompat serviceSwitch = (PrefsSwitchCompat) findViewById(R.id.serviceSwitch);
        serviceSwitch.setEnabled(enabled());
        serviceSwitch.setOnCheckedChangeListener(null);
        serviceSwitch.setChecked(checked());
        serviceSwitch.setOnCheckedChangeListener(this);
    }

    private boolean checked() {
        SharedPreferences prefs = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        return enabled() & prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false);
    }

    private boolean enabled() {
        SharedPreferences prefs = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        return (prefs.getLong(AppSettings.ACTIVE_NOTIFICATION, 0) != 0
                || prefs.getLong(AppSettings.ACTIVE_RINGTONE, 0) != 0)
                && AppSqlHelper.hasData(this, Media.Collection.TABLE);
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
                    AppSettings.changeNotificationSound(v.getContext(), false);
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
                    AppSettings.changeRingtone(v.getContext(), false);
                }
            });
        }
    }

    private void refresh(Uri uri, View view, boolean init) {
        if (uri == null) {
            hide(view.findViewById(R.id.properties), init, false);
            return;
        }
        Cursor cursor = getContentResolver().query(uri, IndexerService.PROJECTION, null, null, null);

        if (cursor == null) {
            hide(view.findViewById(R.id.properties), init, false);
            return;
        }
        if (!cursor.moveToNext()) {
            cursor.close();
            hide(view.findViewById(R.id.properties), init, false);
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
        show(view.findViewById(R.id.properties), init);
    }

    public void loadBitmap(RelativeLayout albumArts, long collectionId, HashMap<Long, TreeSet<MediaItem>> itemsMap) {
        TreeSet<MediaItem> items = itemsMap.get(collectionId);
        albumArts.getLayoutParams().height = mAlbumArtWidth;
        albumArts.getLayoutParams().width = mAlbumArtWidth * 2;

        //default
        for (int i = 0; i < NUM_THUMBNAILS - 1; i++)
            albumArts.getChildAt(i).setVisibility(View.GONE);
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
        intent.putExtra(CollectionPageActivity.ART_MEDIA_ITEM, mediaItem);
        intent.putExtra(CollectionPageActivity.COLLECTION_ID, collectionId);
        if (Utils.hasLollipop()) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageView, getString(R.string.transition_collection_page));
            ((Activity) v.getContext()).startActivityForResult(intent, CollectionPageActivity.GROUP_RETURN_CODE, options.toBundle());
        } else
            ((Activity) v.getContext()).startActivityForResult(intent, CollectionPageActivity.GROUP_RETURN_CODE);
        //finish();
    }

    public static void collapse(final View view, final boolean remove) {
        if (view.getVisibility() == View.GONE)
            return;

        final int initialHeight = view.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
                if (remove) ((ViewGroup) view.getParent()).removeView(view);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // 1dp/ms
        a.setDuration(400);
        view.startAnimation(a);
    }

    private void expand(final View view) {
        if (view.getVisibility() == View.VISIBLE)
            return;
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        view.getLayoutParams().height = 1;
        view.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(400);
        view.startAnimation(a);
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

    @Override
    public void onRFACItemLabelClick(int position, RFACLabelItem item) {
        onRFACItemIconClick(position, item);
    }

    @Override
    public void onRFACItemIconClick(int position, RFACLabelItem item) {
        mRapidFloatingActionHelper.toggleContent();
        switch (position) {
            case 0:
                // This always works
                Intent intent = new Intent(this, FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                intent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(intent, FILE_PICKER_CODE);
                break;
            case 1:
                startActivity(new Intent(AbstractMenuActivity.this, CollectionEditActivity.class));
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILE_PICKER_CODE && resultCode == Activity.RESULT_OK) {
            Uri folderUri = intent.getData();
            if (folderUri == null) {
                Utils.toast(this, "No folder selected!");
            } else {
                AppLogger.wtf(this, folderUri.toString(), new File(folderUri.getPath()).getAbsolutePath());
                long folderId = Media.getFolderId(this, new File(folderUri.getPath()));
                if (folderId == -1) Utils.toast(this, "No indexed-media found in selected folder!");
                else {
                    final long fauxId = System.currentTimeMillis();
                    Intent serviceIntent = new Intent(this, AppService.class);
                    serviceIntent.setType(AppService.SAVE_FOLDER_COLLECTION);
                    serviceIntent.putExtra(AppService.EXTRA_FOLDER, folderId);
                    serviceIntent.putExtra(CollectionEditActivity.COLLECTION_NAME, new File(folderUri.getPath()).getName());
                    serviceIntent.putExtra(AppService.FAUX_ID, fauxId);
                    final ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setCancelable(false);
                    dialog.setMessage(getString(R.string.saving_collection));
                    BroadcastReceiver receiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            unregisterReceiver(this);
                            if (fauxId != intent.getLongExtra(AppService.FAUX_ID, 0))
                                throw new RuntimeException();
                            refresh();
                            dialog.dismiss();
                        }
                    };
                    registerReceiver(receiver, new IntentFilter(AppService.SAVE_FOLDER_COLLECTION));
                    startService(serviceIntent);
                }
            }
        } else
            super.onActivityResult(requestCode, resultCode, intent);
    }
}
