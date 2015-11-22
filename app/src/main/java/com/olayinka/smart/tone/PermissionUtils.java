package com.olayinka.smart.tone;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public final class PermissionUtils {


    public static final int PERMISSION_STORAGE = 0b00001;
    public static final int PERMISSION_PHONE = 0b00010;
    public static final int PERMISSION_BOOT = 0b00100;
    public static final int PERMISSION_WRITE_SETTINGS = 0b01000;
    public static final int PERMISSION_NOTIFICATION = 0b10000;

    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.WRITE_SETTINGS,
            AppSettings.NOTIFICATION_PERMISSION,
    };

    public static boolean[] getAllPermissions(Context context) {
        boolean[] res = new boolean[PERMISSIONS.length];
        for (int i = 0; i < 3; i++) res[i] = hasSelfPermission(context, PERMISSIONS[i]);

        res[3] = hasWriteSettingsPermisssion(context);
        res[4] = hasNotificationPermission(context);

        return res;
    }


    public static int countGrantedPermissions(Context context) {
        boolean[] permissions = getAllPermissions(context);
        int i = 0;
        for (boolean b : permissions) if (b) i++;
        return i;
    }


    /**
     * Check that given permission have been granted.
     */
    public static boolean hasGranted(int grantResult) {
        return grantResult == PERMISSION_GRANTED;
    }

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     */
    public static boolean hasGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (!hasGranted(result)) {
                return false;
            }
        }
        return true;
    }


    public static boolean hasAccessibilityPermission(Context context) {
        ComponentName cn = new ComponentName(context, "com.olayinka.smart.tone.service.NotifAccessibilityService");
        String flat = android.provider.Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return flat != null && flat.contains(cn.flattenToString());
    }

    /**
     * Returns true if the Context has access to a given permission.
     * Always returns true on platforms below M.
     */
    public static boolean hasSelfPermission(Context context, String permission) {
        return !Utils.hasMarshmallow() || permissionHasGranted(context, permission);
    }

    /**
     * Returns true if the Context has access to all given permissions.
     * Always returns true on platforms below M.
     */
    public static boolean hasSelfPermissions(Context context, String[] permissions) {
        if (!Utils.hasMarshmallow()) {
            return true;
        }

        for (String permission : permissions) {
            if (!permissionHasGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Requests permissions to be granted to this application.
     */
    public static void requestPermissions(@NonNull Activity activity, int requestCode, int permissions) {
        if (Utils.hasMarshmallow()) {
            ArrayList<String> permsList = new ArrayList<>(PERMISSIONS.length);
            for (int i = 0; i < PERMISSIONS.length; i++)
                if ((permissions & (1 << i)) > 0)
                    permsList.add(PERMISSIONS[i]);
            String[] perms = new String[permsList.size()];
            if (permsList.size() > 0)
                requestPermissions(activity, requestCode, permsList.toArray(perms));

        }
    }


    /**
     * Requests permissions to be granted to this application.
     */
    public static void requestPermissions(@NonNull Activity activity, int requestCode, @NonNull String... permissions) {
        if (Utils.hasMarshmallow()) {
            internalRequestPermissions(activity, permissions, requestCode);
        }
    }

    private static void internalRequestPermissions(Activity activity, String[] permissions, int requestCode) {
        if (activity == null) {
            throw new IllegalArgumentException("Given activity is null.");
        }
        activity.requestPermissions(permissions, requestCode);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static boolean permissionHasGranted(Context context, String permission) {
        return hasGranted(context.checkSelfPermission(permission));
    }

    public static boolean hasWriteSettingsPermisssion(Context context) {
        return !Utils.hasMarshmallow() || Settings.System.canWrite(context);
    }

    public static boolean hasNotificationAccessPermission(Context context) {
        ComponentName cn = new ComponentName(context, "com.olayinka.smart.tone.service.NotifListenerService");
        String flat = android.provider.Settings.Secure.getString(context.getContentResolver(), AppSettings.ENABLED_NOTIFICATION_LISTENERS);
        return flat != null && flat.contains(cn.flattenToString());
    }

    public static boolean hasNotificationPermission(Context context) {
        return !Utils.hasJellyBeanMR2() ?
                PermissionUtils.hasAccessibilityPermission(context) :
                PermissionUtils.hasNotificationAccessPermission(context);
    }


}