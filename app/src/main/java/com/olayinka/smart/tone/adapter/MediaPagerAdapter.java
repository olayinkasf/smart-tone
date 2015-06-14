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
import lib.olayinka.smart.tone.R;

import java.util.Set;

/**
 * Created by olayinka on 5/1/15.
 */
public class MediaPagerAdapter extends PagerAdapter {

    private Set<Long> mSelection;

    public MediaPagerAdapter(Set<Long> mSelection) {
        this.mSelection = mSelection;
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
        switch (position) {
            case 0:
                return "Selection";
            case 1:
                return "All Media";
            case 2:
                return "Ringtone";
            case 3:
                return "Notification";
            case 4:
                return "Album";
            case 5:
                return "Folder";
        }
        throw new RuntimeException();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null;
        if (position < 4)
            view = LayoutInflater.from(container.getContext()).inflate(R.layout.media_list, container, false);
        else view = LayoutInflater.from(container.getContext()).inflate(R.layout.media_grid, container, false);

        AbsListView mediaListView = (AbsListView) view.findViewById(R.id.list);

        switch (position) {
            case 0:
                mediaListView.setAdapter(new SelectionListAdapter(container.getContext(), mSelection));
                break;
            case 1:
                LayoutInflater inflater = (LayoutInflater) mediaListView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (!alreadyGotIt(container.getContext())) {
                    final ViewGroup header = (ViewGroup) inflater.inflate(R.layout.double_tap_header, mediaListView, false);
                    header.findViewById(R.id.got_it).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            gotIt(v.getContext(), header);
                        }

                    });
                    ((ListView) mediaListView).addHeaderView(header, null, true);
                }
                mediaListView.setAdapter(new MediaListAdapter(container.getContext(), MediaListAdapter.SELECTION_ALL, mSelection));
                break;
            case 2:
                mediaListView.setAdapter(new MediaListAdapter(container.getContext(), MediaListAdapter.SELECTION_RINGTONE, mSelection));
                break;
            case 3:
                mediaListView.setAdapter(new MediaListAdapter(container.getContext(), MediaListAdapter.SELECTION_NOTIFICATION, mSelection));
                break;
            case 4:
                mediaListView.setAdapter(new AlbumListAdapter(container.getContext(), Media.Album.TABLE, mSelection));
                break;
            case 5:
                mediaListView.setAdapter(new FolderListAdapter(container.getContext(), mSelection));
                break;
        }

        container.addView(view);
        return view;
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
