package com.olayinka.smart.tone.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.activity.CollectionPickerActivity;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;
import com.olayinka.smart.tone.task.AsyncTask;
import com.olayinka.smart.tone.widget.CenterTopImageView;
import lib.olayinka.smart.tone.R;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created by Olayinka on 5/3/2015.
 */
public class CollectionListAdapter extends CursorAdapter {

    private HashMap<Long, TreeSet<MediaItem>> mTonesMap;
    private HashMap<Long, ToneLoader> mToneLoaderMap;
    private View.OnClickListener mItemClickListener;

    public CollectionListAdapter(Context context, View.OnClickListener itemClickListener) {
        super(context, AppSqlHelper.instance(context).getReadableDatabase()
                .query(Media.Collection.TABLE, new String[]{"*"}, null, null, null, null, Media.Columns.NAME)
                , false);
        mTonesMap = new HashMap<>(50);
        mToneLoaderMap = new HashMap<>(50);
        mItemClickListener = itemClickListener;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.collection_item, null);
        view.setOnClickListener(mItemClickListener);
        RelativeLayout albumTable = (RelativeLayout) view.findViewById(R.id.albumArt);
        for (int i = 0; i < 3; i++) albumTable.addView(new CenterTopImageView(context));
        ImageView transitionImageView = new CenterTopImageView(context);
        if (Utils.hasLollipop()) {
            transitionImageView.setId(R.id.transitionImage);
            transitionImageView.setTransitionName(context.getString(R.string.transition_collection_page));
        }
        albumTable.addView(transitionImageView);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        view.setTag(R.id.collectionItem, cursor.getLong(0));

        RelativeLayout albumTable = (RelativeLayout) view.findViewById(R.id.albumArt);
        TextView collectionName = (TextView) view.findViewById(R.id.collectionName);

        collectionName.setText(cursor.getString(1));

        long id = cursor.getLong(0);
        if (mToneLoaderMap != null && mToneLoaderMap.get(id) == null) {
            ToneLoader toneLoader = new ToneLoader(id);
            mToneLoaderMap.put(id, toneLoader);
            toneLoader.execute(view.getContext());
        }
        ((CollectionPickerActivity) albumTable.getContext()).loadBitmap(albumTable, id, mTonesMap);
    }

    public TreeSet<MediaItem> getItemsForCollection(long tag) {
        return mTonesMap.get(tag);
    }


    class ToneLoader extends AsyncTask<Object, Void, TreeSet<MediaItem>> {

        long mId;

        public ToneLoader(long mId) {
            this.mId = mId;
        }

        @Override
        protected void onPostExecute(TreeSet<MediaItem> o) {
            mTonesMap.put(mId, o);
            if (mToneLoaderMap != null && getCount() == mToneLoaderMap.size()) {
                mToneLoaderMap.clear();
                mToneLoaderMap = null;
            }
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
            while (cursor.moveToNext() && items.size() < 4) {

                MediaItem mediaItem = new MediaItem(cursor.getLong(0), cursor.getLong(1), cursor.getInt(2));
                if (Utils.isValidUri((Context) params[0], Utils.uriForMediaItem(mediaItem))) {
                    items.add(mediaItem);
                }
            }
            cursor.close();
            return items;
        }
    }
}
