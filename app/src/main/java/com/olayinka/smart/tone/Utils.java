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

package com.olayinka.smart.tone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import com.olayinka.smart.tone.model.Media;
import com.olayinka.smart.tone.model.MediaItem;
import lib.olayinka.smart.tone.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Olayinka on 4/12/2015.
 */
public class Utils {
    public static final String ACTIVE_VALUE = "active.value";
    public static final String TEXT_VALUE = "text.value";
    public static final String APP_PACKAGE_NAME = "com.olayinka.smart.tone";
    public static Map<String, String> VAR_MAP = new HashMap<>(10);
    private static Toast sAppToast;
    private static Bitmap sCachedBitmap;

    public static void squareImageView(Context mContext, ImageView imageView) {
        int width = 0;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (hasHoneycombMR2()) {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        } else {
            width = display.getWidth();  // Deprecated
        }
        imageView.getLayoutParams().width = width;
        imageView.getLayoutParams().height = width;
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasHoneycombMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
    }

    public static boolean hasIceCreamSandwich() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static String format(String text, JSONArray vars) throws JSONException {
        String[] varVals = new String[vars.length()];

        for (int i = 0; i < vars.length(); i++) {
            varVals[i] = VAR_MAP.get(vars.getString(i).trim());
        }

        return String.format(text, varVals);
    }

    public static String getRawString(Context context, int resourceId) {
        InputStream is = context.getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String sep = System.getProperty("line.separator");
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(sep);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return builder.toString();
    }

    public static void validate(Context context, JSONObject jsonObject) throws JSONException {
        JSONObject activeObject = jsonObject.getJSONObject("active");
        if (activeObject.has("value")) {
            jsonObject.put(ACTIVE_VALUE, true);
            jsonObject.put(TEXT_VALUE, activeObject.getString("text"));
            return;
        }
        JSONObject settingsObject = activeObject.getJSONObject("settings");
        boolean active;
        switch (settingsObject.getString("type")) {
            case "database":
                active = AppSqlHelper.hasData(context, settingsObject.getString("table"));
                break;
            case "vars":
                jsonObject.put(ACTIVE_VALUE, true);
                jsonObject.put(TEXT_VALUE, format(activeObject.getString("text"), settingsObject.getJSONArray("vars")));
                return;
            default:
                throw new RuntimeException("Invalid option");
        }

        jsonObject.put(ACTIVE_VALUE, active);
        if (!active) {
            jsonObject.put(TEXT_VALUE, activeObject.getString("negative"));
        } else {
            jsonObject.put(TEXT_VALUE, validateText(context, activeObject.get("text"), null));
        }

    }

    private static String validateText(Context context, Object text, String... supply) throws JSONException {
        if (text instanceof String) {
            return String.format(String.valueOf(text), supply);
        }
        JSONObject textObject = (JSONObject) text;
        JSONObject settingsObject = textObject.getJSONObject("settings");
        String newSupply;
        switch (settingsObject.getString("type")) {
            case "database":
                String table = settingsObject.getString("table");
                String supplyValue = String.valueOf(supply[0]);
                String supplyColumn = settingsObject.getString("supply");
                String column = settingsObject.getString("column");
                Cursor cursor = AppSqlHelper.instance(context)
                        .getReadableDatabase()
                        .query(table, new String[]{column}, supplyColumn + Media.EQUALS, new String[]{supplyValue}, null, null, null);
                if (cursor.getCount() != 1) {
                    return textObject.getString("negative");
                }
                cursor.moveToNext();
                newSupply = cursor.getString(0);
                cursor.close();
                break;
            case "preferences":
                long val = context.getSharedPreferences(settingsObject.getString("name"), Context.MODE_PRIVATE)
                        .getLong(settingsObject.getString("key"), 0);
                if (val == 0) return textObject.getString("negative");
                newSupply = "" + val;
                break;
            default:
                throw new RuntimeException("Error in json file!");
        }
        return validateText(context, textObject.get("text"), newSupply);
    }

    public static void toast(Context context, String msg) {
        if (sAppToast != null)
            sAppToast.cancel();
        sAppToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        sAppToast.show();
    }

    public static void toast(Context context, int resId) {
        toast(context, context.getString(resId));
    }

    public static String serialize(Collection selection) {
        JSONArray jsonArray = new JSONArray();
        for (Object id : selection) {
            jsonArray.put(id);
        }
        return jsonArray.toString();
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static DisplayMetrics displayDimens(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }

    public static boolean isValidUri(Context context, Uri uri) {
        ContentResolver cr = context.getContentResolver();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cur = cr.query(uri, projection, null, null, null);
        if (cur != null && cur.moveToNext()) {
            String filePath = cur.getString(0);
            if (new File(filePath).exists()) {
                cur.close();
                return true;
            }
        }
        if (cur != null) cur.close();
        return false;
    }

    public static Uri uriForMediaItem(MediaItem mediaItem) {
        Uri sArtworkUri = Uri.parse("content://media/" + (mediaItem.getInternal() == 0 ? "external" : "internal") + "/audio/albumart");
        return ContentUris.withAppendedId(sArtworkUri, mediaItem.getAlbumId());
    }

    public static boolean hasPermission(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static void removeNotification(Context context) {
        NotificationManager notifManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
    }

    public static void notify(Context context, String content, int notificationId, PendingIntent intent) {


        if (sCachedBitmap == null)
            sCachedBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_notif_large);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notif_small)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(content)
                .setAutoCancel(true)
                .setLargeIcon(sCachedBitmap)
                .setSound(uri)
                .setTicker(content)
                .setContentIntent(intent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content));
        Notification notification = mBuilder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(notificationId, notification);
    }
}
