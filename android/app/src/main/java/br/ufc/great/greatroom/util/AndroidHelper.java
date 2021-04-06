package br.ufc.great.greatroom.util;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * Created by adyson on 25/11/15.
 */
public class AndroidHelper {

    public static boolean isMinVersion(int version) {
        return Build.VERSION.SDK_INT >= version;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isMinMarshmallow() {
        return isMinVersion(Build.VERSION_CODES.M);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isMinLollipop() {
        return isMinVersion(Build.VERSION_CODES.LOLLIPOP);
    }

    public static boolean isMinKitKat() {
        return isMinVersion(Build.VERSION_CODES.KITKAT);
    }

    public static boolean isMinIceCreamSandwich() {
        return isMinVersion(Build.VERSION_CODES.ICE_CREAM_SANDWICH);
    }

    public static boolean isMinJellyBean() {
        return isMinVersion(Build.VERSION_CODES.JELLY_BEAN);
    }

}