package com.olayinka.smart.tone.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.listener.MenuClickListener;
import lib.olayinka.smart.tone.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by olayinka on 5/1/15.
 */
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.PrefsViewHolder> {

    private final MenuClickListener mMenuClickListener;
    private JSONArray mPrefsJSONArray;

    public MenuAdapter(Context context) {
        readPrefsArray(context);
        mMenuClickListener = new MenuClickListener(mPrefsJSONArray);
    }


    private void readPrefsArray(Context context) {
        String prefsRawString = Utils.getRawString(context, R.raw.menu);
        try {
            mPrefsJSONArray = new JSONArray(prefsRawString);
            for (int i = 0; i < mPrefsJSONArray.length(); i++) {
                Utils.validate(context, mPrefsJSONArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PrefsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PrefsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(PrefsViewHolder holder, int position) {
        try {
            holder.bindView(position);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getItemCount() {
        return mPrefsJSONArray.length();
    }

    class PrefsViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView textView;
        int position;

        public PrefsViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(mMenuClickListener);
            titleView = (TextView) itemView.findViewById(R.id.title);
            textView = (TextView) itemView.findViewById(R.id.text);
        }

        void bindView(int position) throws JSONException {
            this.position = position;

            View parentView = (View) textView.getParent();
            titleView.setEnabled(getJSONObject().getBoolean("active.value"));
            parentView.setEnabled(getJSONObject().getBoolean("active.value"));
            parentView.setTag(R.id.menuItem, position);

            try {
                JSONObject jsonObject = getJSONObject();
                titleView.setText(jsonObject.getString("title"));
                textView.setText(jsonObject.getString("text.value"));

                titleView.setVisibility(View.VISIBLE);
                if (textView.getText().toString().trim().isEmpty()) {
                    textView.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        JSONObject getJSONObject() throws JSONException {
            return mPrefsJSONArray
                    .getJSONObject(position);
        }
    }

}
