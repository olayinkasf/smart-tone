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
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.OrderedMediaSet;
import lib.olayinka.smart.tone.R;

/**
 * Created by olayinka on 5/1/15.
 */
public class MediaPagerAdapter extends PagerAdapter {

    private OrderedMediaSet<Long> mSelection;
    final String[] mHeaders;

    public MediaPagerAdapter(OrderedMediaSet<Long> mSelection, String[] headers) {
        this.mSelection = mSelection;
        mHeaders = headers;
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return o == view;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mHeaders[position];
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null;
        Context context = container.getContext();
        if (position == 0)
            view = LayoutInflater.from(context).inflate(R.layout.drag_media_list, container, false);
        else if (position < 4)
            view = LayoutInflater.from(context).inflate(R.layout.media_list, container, false);
        else view = LayoutInflater.from(context).inflate(R.layout.media_grid, container, false);

        AbsListView mediaListView = (AbsListView) view.findViewById(R.id.list);
        MediaListAdapter mediaListAdapter = null;
        switch (position) {
            case 0: {
                SelectionListAdapter selectionListAdapter = new SelectionListAdapter(context, mSelection);
                mediaListView.setAdapter(selectionListAdapter);
                mSelection.addListener(MediaListAdapter.SELECTION_SELECTED, selectionListAdapter);
                break;
            }
            case 1:
                if (!alreadyGotIt(context)) {
                    LayoutInflater inflater = (LayoutInflater) mediaListView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final ViewGroup header = (ViewGroup) inflater.inflate(R.layout.double_tap_header, mediaListView, false);
                    header.findViewById(R.id.got_it).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            gotIt(v.getContext(), header);
                        }
                    });
                    ((ListView) mediaListView).addHeaderView(header, null, true);
                }
                adaptMediaListView(context, MediaListAdapter.SELECTION_ALL, mediaListView);
                break;
            case 2:
                adaptMediaListView(context, MediaListAdapter.SELECTION_RINGTONE, mediaListView);
                break;
            case 3:
                adaptMediaListView(context, MediaListAdapter.SELECTION_NOTIFICATION, mediaListView);
                break;
            case 4:
                mediaListView.setAdapter(new AlbumListAdapter(context, Media.Album.TABLE, mSelection));
                break;
            case 5:
                mediaListView.setAdapter(new FolderListAdapter(context, mSelection));
                break;
        }

        container.addView(view);
        return view;
    }

    private void adaptMediaListView(Context context, String selection, AbsListView listView) {
        MediaListAdapter mediaListAdapter = new MediaListAdapter(context, selection, mSelection);
        listView.setAdapter(mediaListAdapter);
        mSelection.addListener(selection, mediaListAdapter);
    }

    private void gotIt(Context context, final View header) {
        context.getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE)
                .edit().putBoolean(AppSettings.GOT_IT_DOUBLE_TAP, true).apply();

        final int height = header.getMeasuredHeight();
        final AbsListView.LayoutParams layoutParams = (AbsListView.LayoutParams) header.getLayoutParams();

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
                ((ListView) header.getParent()).removeHeaderView(header);
            }
        });

        set.start();
    }

    private boolean alreadyGotIt(Context context) {
        return context.getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE)
                .getBoolean(AppSettings.GOT_IT_DOUBLE_TAP, false);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}
