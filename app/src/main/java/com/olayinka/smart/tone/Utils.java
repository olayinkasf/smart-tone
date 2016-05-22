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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
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
    private static Bitmap sLargeIcon;

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

    public static boolean hasJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
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

    public static void notify(Context context, String content, int notificationId, PendingIntent intent) {


        if (sCachedBitmap == null)
            sCachedBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_notif_large);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notif_small)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(content)
                .setAutoCancel(true)
                .setLargeIcon(sCachedBitmap)
                .setTicker(content)
                .setContentIntent(intent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content));
        Notification notification = mBuilder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(notificationId, notification);
    }

    public static String readFile(File file) throws IOException {
        BufferedReader br = null;
        try {
            StringBuilder text = new StringBuilder();
            br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            return text.toString();
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    public static void copyFile(File source, File dest) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        }
    }

    public static void appendFile(File source, File dest) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest, true);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        }
    }

    public static File getExternalStorageDir(String dir, String fileName) {
        File file = null;
        if (isExternalStorageWritable()) {
            File sdCard = Environment.getExternalStorageDirectory();
            file = new File(sdCard, "com.olayinka.smart.tone");
            file = new File(file, dir);
            if (!file.exists() && !file.mkdirs())
                return null;
            file = new File(file, fileName);
        }
        return file;
    }


    public static boolean checkWriteSettings(Context context) {
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!PermissionUtils.hasWriteSettingsPermisssion(context)) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_notif_small)
                    .setLargeIcon(getLargeIcon(context))
                    .setContentTitle(context.getString(R.string.smarttone_service))
                    .setContentText(context.getString(R.string.grant_write_settings))
                    .setTicker(context.getString(R.string.grant_write_settings))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.grant_write_settings)))
                    .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS), 0))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            Notification notification = mBuilder.build();
            notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_NO_CLEAR | Notification.FLAG_AUTO_CANCEL;
            mNotifyMgr.notify(R.id.writeSettingsNotif, notification);
            return false;
        } else {
            mNotifyMgr.cancel(R.id.writeSettingsNotif);
            return true;
        }
    }

    public static Bitmap getLargeIcon(Context context) {
        if (sLargeIcon == null) {
            sLargeIcon = BitmapFactory.decodeResource(context.getResources(), lib.olayinka.smart.tone.R.mipmap.ic_notif_large);
        }
        return sLargeIcon;
    }
}
