package com.olayinka.smart.tone;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
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
import java.util.Random;

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

    public static void notify(Context context, String content) {
        Random random = new Random();
        int notificationId = random.nextInt(9999 - 1000) + 1000;

        if (sCachedBitmap == null)
            sCachedBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.large_notif_icon);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.small_notif_icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(content)
                .setAutoCancel(true)
                .setLargeIcon(sCachedBitmap)
                .setTicker(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content));
        Notification notification = mBuilder.build();
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(notificationId, notification);
    }
/*
    public static Bitmap fastBlur(Bitmap sentBitmap, int radius) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com

        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.

        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.

        // If you are using this algorithm in your code please add
        // the following line:
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }
    */
}
