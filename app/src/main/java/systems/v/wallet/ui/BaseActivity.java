package systems.v.wallet.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import retrofit2.HttpException;
import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.ui.view.SplashActivity;
import systems.v.wallet.ui.view.VerifyActivity;
import systems.v.wallet.ui.widget.LoadingDialog;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.utils.bus.AppBus;

public abstract class BaseActivity extends AppCompatActivity {

    private static final List<String> VERIFY_UNCHECK_ACTIVITIES =
            Collections.unmodifiableList(Arrays.asList(
                    SplashActivity.class.getSimpleName(),
                    VerifyActivity.class.getSimpleName()
            ));

    protected Activity mActivity;
    protected Handler mHandler;
    protected App mApp;
    protected LoadingDialog mLoading;
    protected Wallet mWallet;
    private boolean isAppResume = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        mLoading = new LoadingDialog(mActivity);
        mApp = App.getInstance();
        mHandler = new Handler();
        mWallet = mApp.getWallet();
        AppBus.register(this);
    }

    @Override
    protected void onStart() {
        isAppResume = !mApp.isAppVisible();
        super.onStart();
    }

    @Override
    protected void onResume() {
        check();
        isAppResume = false;
        mApp.setLanguageChange(false);
        super.onResume();
    }

    private void check() {
        if (VERIFY_UNCHECK_ACTIVITIES.contains(getClass().getSimpleName())) {
            return;
        }
        if (!FileUtil.walletExists(mActivity)) {
            return;
        }
        if (mApp.isLanguageChange()) {
            return;
        }
        if (isAppResume || mApp.getWallet() == null) {
            VerifyActivity.launch(mActivity);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoading != null) {
            mLoading.dismissLoading();
            mLoading = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mApp.stopLockCounter();
        return super.dispatchTouchEvent(ev);
    }

    protected void setAppBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    protected void fillStatusViewWithColor(@ColorRes int color) {
        ViewGroup contentView = findViewById(android.R.id.content);
        View statusBarView = new View(mActivity);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight());
        statusBarView.setBackgroundColor(ContextCompat.getColor(this, color));
        contentView.addView(statusBarView, lp);
    }

    protected void fillStatusViewWithDrawable(@DrawableRes int drawable) {
        ViewGroup contentView = findViewById(android.R.id.content);
        View statusBarView = new View(mActivity);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight());
        statusBarView.setBackground(ContextCompat.getDrawable(this, drawable));
        contentView.addView(statusBarView, lp);
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    protected void fullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                View decorView = window.getDecorView();
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorView.setSystemUiVisibility(option);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
//                window.setNavigationBarColor(Color.TRANSPARENT);
            } else {
                Window window = getWindow();
                WindowManager.LayoutParams attributes = window.getAttributes();
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
                attributes.flags |= flagTranslucentStatus;
//                attributes.flags |= flagTranslucentNavigation;
                window.setAttributes(attributes);
            }
        }
    }

    protected static void openAnimVertical(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_open_in_anim_vertical, R.anim.activity_anim_static);
    }

    protected void closeAnimVertical(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_anim_static, R.anim.activity_close_out_anim_vertical);
    }

    protected static void openAnimHorizontal(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_open_in_anim_horizontal, R.anim.activity_anim_static);
    }

    protected void closeAnimHorizontal(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_anim_static, R.anim.activity_close_out_anim_horizontal);
    }

    protected static void openAlpha(Activity activity) {
        activity.overridePendingTransition(R.anim.anim_alph_start, R.anim.activity_anim_static);
    }

    protected void closeAlpha(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_anim_static, R.anim.anim_alph_close);
    }

    protected void showLoading() {
        if (mLoading != null) {
            mLoading.showLoading();
        }
    }

    protected void hideLoading() {
        if (mLoading != null) {
            mLoading.dismissLoading();
        }
    }

    public <O> ObservableTransformer<O, O> bindLoading() {
        return new ObservableTransformer<O, O>() {
            private void hide() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                    }
                });
            }

            @Override
            public Observable<O> apply(Observable<O> observable) {
                return observable.doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showLoading();
                            }
                        });
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        hide();
                    }
                }).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        hide();
                        if (throwable instanceof HttpException) {
                            try {
                                String msg = ((HttpException) throwable).response().errorBody().string();
                                Log.d("HTTP error", msg);
                            } catch (IOException e) {

                            }
                        }
                    }
                }).doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        hide();
                    }
                });
            }
        };
    }

    protected void showToast(@StringRes int res) {
        ToastUtil.showToast(res);
    }

    public static void showToast(String text) {
        ToastUtil.showToast(text);
    }
}
