package com.olayinka.smart.tone.listener;

import android.content.Intent;
import android.view.View;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.activity.*;
import com.olayinka.smart.tone.service.AppService;
import lib.olayinka.smart.tone.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Olayinka on 4/12/2015.
 */
public class MenuClickListener implements View.OnClickListener {

    private JSONArray mPrefsJSONArray;

    public MenuClickListener(JSONArray mPrefsJSONArray) {
        this.mPrefsJSONArray = mPrefsJSONArray;
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag(R.id.menuItem);
        try {
            JSONObject object = mPrefsJSONArray.getJSONObject(position);
            switch (object.getString("key")) {
                case "call.ringtone":
                    v.getContext().startActivity(new Intent(v.getContext(), RingtoneSelectActivity.class));
                    break;
                case "notification.sound":
                    v.getContext().startActivity(new Intent(v.getContext(), NotificationSelectActivity.class));
                    break;
                case "manage.collections":
                    v.getContext().startActivity(new Intent(v.getContext(), CollectionToManageActivity.class));
                    break;
                case "app.settings":
                    v.getContext().startActivity(new Intent(v.getContext(), SettingsActivity.class));
                    break;
                case "export.collections":
                    Intent intent = new Intent(v.getContext(), AppService.class);
                    intent.setType(AppService.EXPORT_COLLECTIONS);
                    Utils.toast(v.getContext(), R.string.collection_export_sent);
                    v.getContext().startService(intent);
                    break;
                case "app.about":
                    v.getContext().startActivity(new Intent(v.getContext(), AboutAppActivity.class));
                    break;
                case "app.licenses":
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }
}
