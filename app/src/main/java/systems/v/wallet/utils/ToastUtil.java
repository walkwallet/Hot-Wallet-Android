package systems.v.wallet.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.StringRes;
import systems.v.wallet.App;

public class ToastUtil {

    private static int oldMsg;

    private static Toast toast = null;

    private static long oneTime = 0;

    private static long twoTime = 0;

    public static void showToast(@StringRes int resId) {
        if (toast == null) {
            toast = Toast.makeText(App.getInstance().getApplicationContext(), resId, Toast.LENGTH_SHORT);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (resId == oldMsg) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = resId;
                toast.setText(resId);
                toast.show();
            }
        }
        oneTime = twoTime;
    }

    public static void showToast(String text) {
        showToast(App.getInstance().getApplicationContext(), Toast.LENGTH_SHORT, text);
    }

    public static void showLongToast(@StringRes int resId) {
        showToast(App.getInstance().getApplicationContext(), Toast.LENGTH_LONG, resId);
    }

    public static void showLongToast(String text) {
        showToast(App.getInstance().getApplicationContext(), Toast.LENGTH_LONG, text);
    }

    public static void showToast(Context ctx, @StringRes int resId) {
        showToast(ctx, Toast.LENGTH_SHORT, resId);
    }

    public static void showToast(Context ctx, String text) {
        showToast(ctx, Toast.LENGTH_SHORT, text);
    }


    public static void showLongToast(Context ctx, @StringRes int resId) {
        showToast(ctx, Toast.LENGTH_LONG, resId);
    }

    public static void showLongToast(Context ctx, String text) {
        showToast(ctx, Toast.LENGTH_LONG, text);
    }

    public static void showToast(Context ctx, int duration, int resId) {
        showToast(ctx, duration, ctx.getString(resId));
    }

    public static void showToast(Context ctx, int duration, String text) {
        Toast.makeText(ctx, text, duration).show();
    }

    public static void showToastOnUiThread(final Activity ctx, final String text) {
        if (ctx != null) {
            ctx.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    showToast(ctx, text);
                }
            });
        }
    }

}
