package com.olayinka.smart.tone.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.PermissionUtils;
import com.olayinka.smart.tone.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 11/21/2015.
 */
public class PermissionsActivity extends AppCompatActivity {

    public static final short PERMISSION_REQUEST_CODE = 0x4289;
    public static final String REQUESTED_PERMISSIONS = "requested.permissions";
    public static final String CLASS_NAME = "class.name";


    private static final int WRITE_SETTINGS_INDEX = 3;
    public static final String PENDING_INTENT_TYPE = "pending.intent.type";
    public static final String ACTION_FAILED = "action.failed";
    public static final String PERMISSION_RESULT = "permission.result";

    JSONArray mPermissions;

    int mRequestedPermissions;
    private ListView mListView;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mPermissions = new JSONArray(Utils.getRawString(this, R.raw.permissions));
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        setContentView(R.layout.permissions);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.permissions);
        mListView = (ListView) findViewById(R.id.list);
        mRequestedPermissions = getIntent().getIntExtra(REQUESTED_PERMISSIONS, 0);
        if (mRequestedPermissions != 0) {
            PermissionUtils.requestPermissions(this, PERMISSION_REQUEST_CODE, mRequestedPermissions);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mListView.setAdapter(new PermissionsAdapter());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            mListView.setAdapter(new PermissionsAdapter());
        }
    }

    private class PermissionsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return PermissionUtils.PERMISSIONS.length;
        }

        @Override
        public Object getItem(int position) {
            try {
                return mPermissions.get(position);
            } catch (JSONException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.permission_item, null);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        showPermissionDialog(position);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            });

            try {
                JSONObject permission = mPermissions.getJSONObject(position);
                TextView titleTextView = (TextView) convertView.findViewById(R.id.title);
                TextView textTextView = (TextView) convertView.findViewById(R.id.text);
                final SwitchCompat switchCompat = (SwitchCompat) convertView.findViewById(R.id.switchCompat);
                titleTextView.setText(permission.getString("title"));
                textTextView.setText(permission.getString("text"));
                if (position < WRITE_SETTINGS_INDEX) {
                    switchCompat.setChecked(PermissionUtils.hasSelfPermission(parent.getContext(), PermissionUtils.PERMISSIONS[position]));
                    switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            boolean granted = PermissionUtils.hasSelfPermission(buttonView.getContext(), PermissionUtils.PERMISSIONS[position]);
                            if (granted) {
                                switchCompat.setChecked(true);
                            } else if (isChecked) {
                                PermissionUtils.requestPermissions(PermissionsActivity.this, PERMISSION_REQUEST_CODE, PermissionUtils.PERMISSIONS[position]);
                            }
                        }
                    });
                } else if (position == WRITE_SETTINGS_INDEX) {
                    switchCompat.setChecked(PermissionUtils.hasWriteSettingsPermisssion(parent.getContext()));
                    switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (PermissionUtils.hasWriteSettingsPermisssion(buttonView.getContext())) {
                                switchCompat.setChecked(true);
                            } else {
                                try {
                                    AlertDialog dialog = showPermissionDialog(position);
                                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS));
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });
                } else {
                    switchCompat.setChecked(PermissionUtils.hasNotificationPermission(parent.getContext()));
                    switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            boolean granted = !Utils.hasJellyBeanMR2() ?
                                    PermissionUtils.hasAccessibilityPermission(buttonView.getContext()) :
                                    PermissionUtils.hasNotificationAccessPermission(buttonView.getContext());
                            if (granted) {
                                switchCompat.setChecked(true);
                            } else if (isChecked) {
                                try {
                                    AlertDialog dialog = showPermissionDialog(position);
                                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            startActivity(new Intent(!Utils.hasJellyBeanMR2() ? Settings.ACTION_ACCESSIBILITY_SETTINGS : AppSettings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            return convertView;
        }


    }

    private AlertDialog showPermissionDialog(int position) throws JSONException {
        return new AlertDialog.Builder(this)
                .setMessage(mPermissions.getJSONObject(position).getString("text.large"))
                .setPositiveButton(R.string.done, null)
                .show();
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(PERMISSION_RESULT, true);
        if (getIntent() != null) {
            if (mRequestedPermissions > 0) {
                boolean[] permissions = PermissionUtils.getAllPermissions(this);
                for (int i = 0; i < PermissionUtils.PERMISSIONS.length; i++)
                    if ((mRequestedPermissions & (1 << i)) > 0 && !permissions[i]) {
                        returnIntent.putExtra(PERMISSION_RESULT, false);
                        break;
                    }
            }
        }
        setResult(Activity.RESULT_OK, returnIntent);
        super.finish();
    }

}