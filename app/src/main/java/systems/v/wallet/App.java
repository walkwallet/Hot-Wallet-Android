package systems.v.wallet;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.multidex.MultiDex;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.ui.view.VerifyActivity;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.SPUtils;

public class App extends Application {
    private static final String TAG = "App";

    private static App mInstance;

    // The list of all mActivities.
    private LinkedList<Activity> mActivities = new LinkedList<>();

    // The top activity is resumed to user.
    private WeakReference<Activity> mTopActivity;
    private int mResumed, mPaused, mStarted, mStopped;
    // Time of the last paused.
    private long mLastPausedTime;
    private Wallet mWallet;
    private Disposable mLockDisposable;
    private int mCounter = 0;
    private boolean mLanguageChange = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        MultiDex.install(this);
        registerActivity();
    }


    public static synchronized App getInstance() {
        return mInstance;
    }

    /**
     * Register the lifecycle callbacks of activity
     */
    private void registerActivity() {
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            // I use four separate variables here. You can, of course, just use two and
            // increment/decrement them instead of using four and incrementing them all.

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.v(TAG, "onActivityCreated " + activity.getClass().getSimpleName());
                Resources resources = activity.getResources();
                Configuration config = resources.getConfiguration();
                DisplayMetrics dm = resources.getDisplayMetrics();
              /*  if (ConfigManager.getLanguageSetting() == 0) {
                    config.locale = Locale.CHINA;
                } else {
                    config.locale = Locale.ENGLISH;
                }*/
                int languageType = SPUtils.getInt(Constants.LANGUAGE, -1);
                if (languageType == -1) {
                    Locale locale = getResources().getConfiguration().locale;
                    String langStr = locale.getLanguage();
                    if (langStr.contains(Locale.SIMPLIFIED_CHINESE.getLanguage())) {
                        languageType = Constants.LAN_ZH_CN;
                    } else if (langStr.contains(Locale.KOREAN.getLanguage())){
                        languageType = Constants.LAN_KO;
                    } else{
                        languageType = Constants.LAN_EN_US;
                    }
                    SPUtils.setInt(Constants.LANGUAGE, languageType);
                }
                if (languageType == Constants.LAN_EN_US) {
                    config.locale = Locale.ENGLISH;
                } else if (languageType == Constants.LAN_ZH_CN) {
                    config.locale = Locale.SIMPLIFIED_CHINESE;
                } else if (languageType == Constants.LAN_KO){
                    config.locale = Locale.KOREAN;
                }
                resources.updateConfiguration(config, dm);
                addActivity(activity);
                startLockCounter();
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.v(TAG, "onActivityDestroyed " + activity.getClass().getSimpleName());
                removeActivity(activity);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Log.v(TAG, "onActivityResumed " + activity.getClass().getSimpleName());
                resumeActivity(activity);
                ++mResumed;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.v(TAG, "onActivityPaused " + activity.getClass().getSimpleName());
                ++mPaused;
                mLastPausedTime = System.currentTimeMillis();
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                Log.v(TAG, "onActivitySaveInstanceState " + activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.v(TAG, "onActivityStarted " + activity.getClass().getSimpleName());
                ++mStarted;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.v(TAG, "onActivityStopped " + activity.getClass().getSimpleName());
                ++mStopped;
            }
        });
    }


    /**
     * @return The milliseconds time of activity's last call {@link Activity #onPause()}
     */
    public long getLastPausedTime() {
        return mLastPausedTime;
    }

    /**
     * @return When the application is resumed to user, return true.
     */
    public boolean isAppVisible() {
        return mStarted > mStopped;
    }


    /**
     * Add an activity to the record list.
     */
    public void addActivity(Activity activity) {
        mActivities.add(activity);
    }


    /**
     * @return The record list.
     */
    public LinkedList<Activity> getActivities() {
        return mActivities;
    }


    /**
     * Remove an activity from the record list.
     */
    public void removeActivity(Activity activity) {
        mActivities.remove(activity);
    }


    /**
     * Record the activity use {@link #mTopActivity}.
     * Must call this method in activity's {@link Activity #onResume()}
     */
    public void resumeActivity(Activity activity) {
        mTopActivity = new WeakReference<>(activity);
    }


    /**
     * @return The top activity, maybe null.
     */
    public Activity getTopActivity() {
        return mTopActivity.get();
    }


    /**
     * @param cls The class must extends {@link Activity} or subclass of it.
     * @return If the activity is exist in {@link #mActivities}, return true.
     */
    public boolean isExist(Class<?> cls) {
        for (Activity activity : mActivities) {
            if (activity.getClass().getSimpleName().equals(cls.getSimpleName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Finish mActivities in an appointed list.
     *
     * @param activities Activities to be finished.
     */
    public void finishActivity(Activity... activities) {
        for (Activity activity : activities) {
            if (activity != null) {
                activity.finish();
            }
        }
    }


    /**
     * Finish all mActivities except an appointed list.
     *
     * @param except The exceptional list.
     */
    public void finishAllActivities(Class<?>... except) {
        for (Activity activity : mActivities) {
            if (activity != null) {
                for (Class<?> c : except) {
                    if (!activity.getClass().getName().equals(c.getName())) {
                        activity.finish();
                    }
                }
            }
        }
    }

    /**
     * Pop and finish activities.
     *
     * @param count The count of Activity.
     */
    public void popActivity(int count) {
        int lastIndex = mActivities.size() - 1;
        for (int i = 0; i < count; i++) {
            Activity activity = mActivities.get(lastIndex - i);
            if (activity != null) {
                activity.finish();
            }
        }
    }

    /**
     * Finish all mActivities.
     */
    public void finishAllActivities() {
        for (Activity activity : mActivities) {
            if (activity != null) {
                activity.finish();
            }
        }
    }


    /**
     * Finish activities on top of.
     */
    public void finishTopActivities(Class<?> util) {
        int size = mActivities.size();
        for (int i = size - 1; i >= 0; i--) {
            Activity activity = mActivities.get(i);
            if (activity.getClass().getName().equals(util.getName())) {
                break;
            }
            activity.finish();
        }
    }


    /**
     * Recreate all mActivities.
     */
    public void recreateActivities() {
        for (Activity activity : mActivities) {
            if (activity != null) {
                activity.recreate();
            }
        }
    }

    /**
     * Stop a service.
     */
    public void stopService(Class<?> cls) {
        stopService(new Intent(this, cls));
    }

    /**
     * Start a service.
     */
    public void startService(Class<?> cls) {
        startService(new Intent(this, cls));
    }

    /**
     * Exit application.
     */
    public void exit() {
        if (mLockDisposable != null) {
            if (!mLockDisposable.isDisposed()) {
                mLockDisposable.dispose();
            }
            mLockDisposable = null;
        }
        finishAllActivities();
        System.exit(0);
    }

    public Wallet getWallet() {
        return mWallet;
    }

    public void setWallet(Wallet wallet) {
        this.mWallet = wallet;
        // set retrofit
        if (wallet != null) {
            RetrofitHelper.setNetwork(wallet.getNetwork());
        }
    }

    private void startLockCounter() {
        if (mLockDisposable != null) {
            return;
        }
        final int finalLockTime = SPUtils.getInt(Constants.AUTO_LOCK, Constants.AUTOLOCK_5);
        mLockDisposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        mCounter++;
                        if (mCounter > finalLockTime) {
                            if (mWallet != null && isAppVisible()
                                    && mTopActivity != null && !(mTopActivity.get() instanceof VerifyActivity)) {
                                VerifyActivity.launch(App.getInstance());
                                mLockDisposable.dispose();
                                mLockDisposable = null;
                            } else {
                                mCounter = 0;
                            }
                        }
                    }
                });
    }

    public void stopLockCounter() {
        mCounter = 0;
        if (!mLockDisposable.isDisposed()) {
            mLockDisposable.dispose();
        }
        mLockDisposable = null;
        startLockCounter();
    }

    public boolean isLanguageChange() {
        return mLanguageChange;
    }

    public void setLanguageChange(boolean languageChange) {
        this.mLanguageChange = languageChange;
    }
}
