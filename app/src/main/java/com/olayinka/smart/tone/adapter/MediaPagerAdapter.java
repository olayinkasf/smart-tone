package com.olayinka.smart.tone.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }


}
