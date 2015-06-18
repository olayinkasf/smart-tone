package com.olayinka.smart.tone.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import com.olayinka.smart.tone.AbsSmartTone;
import com.olayinka.smart.tone.AppSettings;
import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.adapter.MenuAdapter;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.widget.PrefsSwitchCompat;
import lib.olayinka.smart.tone.BuildConfig;
import lib.olayinka.smart.tone.R;

import java.util.Map;

/**
 * Created by Olayinka on 4/7/2015.
 */
public abstract class AbsMenuActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String CONCRETE = Utils.APP_PACKAGE_NAME + ".activity.MenuActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        getVersion();
        setActionBar();
        setCreateButtonClickListener();
    }

    protected abstract void getVersion(Map<String, String> varMap);

    protected void getVersion() {
        Map<String, String> varMap = Utils.VAR_MAP;
        varMap.put("build.time", Long.toHexString(BuildConfig.BUILD_TIME));
        varMap.put("build.number", String.valueOf(BuildConfig.BUILD_NUMBER));
        getVersion(varMap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        inflateRecyclerView();
        setServiceSwitchButton();
    }

    private void setCreateButtonClickListener() {
        findViewById(R.id.createCollection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AbsMenuActivity.this, CollectionEditActivity.class));
            }
        });
    }

    private void setServiceSwitchButton() {
        SharedPreferences prefs = getSharedPreferences(AppSettings.APP_SETTINGS, MODE_PRIVATE);
        PrefsSwitchCompat serviceSwitch = (PrefsSwitchCompat) findViewById(R.id.serviceSwitch);
        boolean enabled = (prefs.getLong(AppSettings.ACTIVE_NOTIFICATION, 0) != 0
                || prefs.getLong(AppSettings.ACTIVE_RINGTONE, 0) != 0)
                && AppSqlHelper.hasData(this, Media.Collection.TABLE);
        serviceSwitch.setEnabled(enabled);
        serviceSwitch.setChecked(enabled & prefs.getBoolean(AppSettings.ACTIVE_APP_SERVICE, false));
        serviceSwitch.setOnCheckedChangeListener(this);
    }

    private void inflateRecyclerView() {
        MenuAdapter mAdapter = new MenuAdapter(this);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(R.string.app_name);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ((AbsSmartTone) getApplication()).startServices();
    }
}
