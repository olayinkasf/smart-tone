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

package com.olayinka.rate.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

/**
 * Created by Olayinka on 7/18/2015.
 */
public class RateThisAppAlert extends AlertDialog.Builder {


    Context mContext;
    boolean mShowRemind = false;

    public static final int GREATER = 3;
    public static final int LESS = 2;
    public static final int EQUALS = 1;
    public static final int NOT_EQUALS = 0;

    private static long LAUNCH_FREQ = -1;

    DialogInterface.OnClickListener mPositiveListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getPackageName())));
            mContext.getSharedPreferences("com.olayinka.rate.app", Context.MODE_PRIVATE)
                    .edit().putBoolean("app.already.rated", true).commit();
            if (mPositiveListenerPlus != null) {
                mPositiveListenerPlus.onClick(dialog, which);
            }
        }
    };
    DialogInterface.OnClickListener mPositiveListenerPlus;

    DialogInterface.OnClickListener mNegativeListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mContext.getSharedPreferences("com.olayinka.rate.app", Context.MODE_PRIVATE)
                    .edit().putBoolean("app.already.rated", true).commit();
            if (mNegativeListenerPlus != null) {
                mNegativeListenerPlus.onClick(dialog, which);
            }
        }
    };
    DialogInterface.OnClickListener mNegativeListenerPlus;

    DialogInterface.OnClickListener mRemindListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mContext.getSharedPreferences("com.olayinka.rate.app", Context.MODE_PRIVATE)
                    .edit().putLong("dialog.last.shown", System.currentTimeMillis()).commit();
            if (mRemindListenerPlus != null) {
                mRemindListenerPlus.onClick(dialog, which);
            }
        }
    };
    DialogInterface.OnClickListener mRemindListenerPlus;

    private boolean mShouldShow = true;
    private boolean mShouldRemind;

    public RateThisAppAlert(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setNegativeButton(R.string.rate_this_app_negative);
        setPositiveButton(R.string.rate_this_app_positive);
        setMessage(String.format(context.getString(R.string.rate_this_app_message), context.getString(R.string.app_name)));
        setCancelable(false);
        if (LAUNCH_FREQ == -1) {
            LAUNCH_FREQ = mContext.getSharedPreferences("com.olayinka.rate.app", Context.MODE_PRIVATE).getLong("launch.freq", 0l);
            context.getSharedPreferences("com.olayinka.rate.app", Context.MODE_PRIVATE).edit().putLong("launch.freq", LAUNCH_FREQ + 1).commit();
        }
    }

    public RateThisAppAlert(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    public RateThisAppAlert remind(boolean toRemind, int numOfDays) {
        mShowRemind = toRemind;
        if (mShowRemind) {
            mShouldRemind = (System.currentTimeMillis() -
                    mContext.getSharedPreferences("com.olayinka.rate.app", Context.MODE_PRIVATE)
                            .getLong("dialog.last.shown", 0)) >= (numOfDays * 24 * 60 * 60 * 1000);
            setRemindButton(R.string.rate_this_app_remind);
        }
        return this;
    }


    @Override
    public AlertDialog show() {
        return show(false);
    }

    public AlertDialog show(boolean forceShow) {
        if (forceShow)
            return super.show();
        boolean alreadyRated = mContext.getSharedPreferences("com.olayinka.rate.app", Context.MODE_PRIVATE)
                .getBoolean("app.already.rated", false);
        if (!alreadyRated && (mShouldShow && (!mShowRemind || mShouldRemind)))
            return super.show();
        else
            return super.create();
    }

    public RateThisAppAlert setPositiveButton(CharSequence text) {
        return setPositiveButton(text, null);
    }

    public RateThisAppAlert setPositiveButton(int textId) {
        return setPositiveButton(textId, null);
    }

    public RateThisAppAlert setNegativeButton(CharSequence text) {
        return setNegativeButton(text, null);
    }

    public RateThisAppAlert setNegativeButton(int textId) {
        return setNegativeButton(textId, null);
    }

    public RateThisAppAlert setRemindButton(CharSequence text) {
        return setRemindButton(text, null);
    }

    public RateThisAppAlert setRemindButton(int textId) {
        return setRemindButton(textId, null);
    }


    @Override
    public RateThisAppAlert setCancelable(boolean cancelable) {
        return (RateThisAppAlert) super.setCancelable(false);
    }

    @Override
    public RateThisAppAlert setPositiveButton(CharSequence text, DialogInterface.OnClickListener addListener) {
        mPositiveListenerPlus = addListener;
        return (RateThisAppAlert) super.setPositiveButton(text, mPositiveListener);
    }

    @Override
    public RateThisAppAlert setPositiveButton(int textId, DialogInterface.OnClickListener addListener) {
        return setPositiveButton(mContext.getString(textId), addListener);
    }


    @Override
    public RateThisAppAlert setNegativeButton(CharSequence text, DialogInterface.OnClickListener addListener) {
        mNegativeListenerPlus = addListener;
        return (RateThisAppAlert) super.setNegativeButton(text, mNegativeListener);
    }

    @Override
    public RateThisAppAlert setNegativeButton(int textId, DialogInterface.OnClickListener addListener) {
        return setNegativeButton(mContext.getString(textId), addListener);
    }

    public RateThisAppAlert setRemindButton(CharSequence text, DialogInterface.OnClickListener addListener) {
        mShowRemind = true;
        mRemindListenerPlus = addListener;
        return (RateThisAppAlert) super.setNeutralButton(text, mRemindListener);
    }

    public RateThisAppAlert setRemindButton(int textId, DialogInterface.OnClickListener addListener) {
        return setRemindButton(mContext.getString(textId), addListener);
    }

    @Override
    public RateThisAppAlert setNeutralButton(CharSequence text, DialogInterface.OnClickListener addListener) {
        return setRemindButton(text, addListener);
    }

    @Override
    public RateThisAppAlert setNeutralButton(int textId, DialogInterface.OnClickListener addListener) {
        return setRemindButton(textId, addListener);
    }

    public RateThisAppAlert addCondition(boolean condition) {
        mShouldShow &= condition;
        return this;
    }

    public RateThisAppAlert addPrefsCondition(String prefsName, String prefsKey) {
        mShouldShow &= mContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE).getBoolean(prefsKey, false);
        return this;
    }

    public RateThisAppAlert addPrefsCondition(String prefsName, String prefsKey, String val) {
        mShouldShow &= equals(mContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE).getString(prefsKey, null), val);
        return this;
    }

    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public RateThisAppAlert addPrefsCondition(String prefsName, String prefsKey, long val, int compare) {
        long memVal = mContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE).getLong(prefsKey, -1L);
        switch (compare) {
            case GREATER:
                mShouldShow &= memVal > val;
                break;
            case LESS:
                mShouldShow &= memVal < val;
                break;
            case EQUALS:
                mShouldShow &= memVal == val;
                break;
            case NOT_EQUALS:
                mShouldShow &= memVal != val;
                break;
            default:
                throw new RuntimeException("Invalid compare condition = " + compare);
        }
        return this;
    }

    public RateThisAppAlert addPrefsCondition(String prefsName, String prefsKey, int val, int compare) {
        int memVal = mContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE).getInt(prefsKey, -1);
        switch (compare) {
            case GREATER:
                mShouldShow &= memVal > val;
                break;
            case LESS:
                mShouldShow &= memVal < val;
                break;
            case EQUALS:
                mShouldShow &= memVal == val;
                break;
            case NOT_EQUALS:
                mShouldShow &= memVal != val;
                break;
            default:
                throw new RuntimeException("Invalid compare condition = " + compare);
        }
        return this;
    }


    public AlertDialog launchInterval(int interval) {
        LAUNCH_FREQ = mContext.getSharedPreferences("com.olayinka.rate.app", Context.MODE_PRIVATE).getLong("launch.freq", 0l);
        mShouldShow = (LAUNCH_FREQ > 0 && LAUNCH_FREQ % interval == 0);
        return show();
    }

}
