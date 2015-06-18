package com.olayinka.smart.tone.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;
import com.olayinka.smart.tone.AppSettings;
import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 4/12/2015.
 */
public class PrefsTextView extends TextView {

    String mPrefsName;
    String mPrefsKey;

    public PrefsTextView(Context context) {
        super(context);
    }

    public PrefsTextView(Context context, AttributeSet attrs) {
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

    }

    public void setPrefs(String name, String key) {
        mPrefsName = name;
        mPrefsKey = key;
    }

    public void setPrefsText() {
        setText(getContext().getSharedPreferences(mPrefsName, Context.MODE_PRIVATE).getString(mPrefsKey + AppSettings.TEXT, ""));
    }

    public String getPrefsValueKey() {
        return mPrefsKey;
    }
}
