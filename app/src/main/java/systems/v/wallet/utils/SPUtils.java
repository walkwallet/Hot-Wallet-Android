package systems.v.wallet.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import systems.v.wallet.App;

public class SPUtils {

    public static SharedPreferences getAppSp() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance());
    }

    public static void setString(String key, String value) {
        getAppSp().edit().putString(key, value).apply();
    }

    public static String getString(String key) {
        return getAppSp().getString(key, "");
    }

    public static void setInt(String key, int value) {
        getAppSp().edit().putInt(key, value).apply();
    }

    public static int getInt(String key) {
        return getAppSp().getInt(key, -1);
    }

    public static int getInt(String key, int defaultVal) {
        return getAppSp().getInt(key, defaultVal);
    }

    public static void setBoolean(String key, boolean value) {
        getAppSp().edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key) {
        return getAppSp().getBoolean(key, true);
    }
    public static boolean getBoolean(String key,boolean defaultVal) {
        return getAppSp().getBoolean(key, defaultVal);
    }
    public static void clear() {
        getAppSp().edit().clear().apply();
    }
}
