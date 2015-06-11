package com.olayinka.smart.tone.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 4/12/2015.
 */
public class PrefsSwitchCompat extends SwitchCompat {

    String mPrefsName;
    String mPrefsKey;

    public PrefsSwitchCompat(Context context) {
        super(context);
    }

    public PrefsSwitchCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Prefs,
                0, 0);

        try {
            mPrefsName = a.getString(R.styleable.Prefs_prefs_name);
            mPrefsKey = a.getString(R.styleable.Prefs_prefs_key);
        } finally {
            a.recycle();
        }

        setChecked(getContext().getSharedPreferences(mPrefsName, Context.MODE_PRIVATE).getBoolean(mPrefsKey, false));
    }

    public void setPrefs(String name, String key) {
        mPrefsName = name;
        mPrefsKey = key;
        setChecked(getContext().getSharedPreferences(mPrefsName, Context.MODE_PRIVATE).getBoolean(mPrefsKey, false));
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        getContext().getSharedPreferences(mPrefsName, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(mPrefsKey, isChecked())
                .apply();
    }
}
