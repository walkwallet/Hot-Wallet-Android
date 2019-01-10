package systems.v.wallet.ui.view.main;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.databinding.ActivityMainBinding;
import systems.v.wallet.ui.BaseActivity;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.SPUtils;
import systems.v.wallet.utils.bus.AppBus;
import systems.v.wallet.utils.bus.AppEvent;
import systems.v.wallet.utils.bus.AppEventType;

public class MainActivity extends BaseActivity {

    public static final int ADD_COLD_ACCOUNT_REQUEST_CODE = 1;

    public static void launch(Activity from, boolean closeOther) {
        if (closeOther) {
            App.getInstance().finishAllActivities(MainActivity.class);
        }
        Intent intent = new Intent(from, MainActivity.class);
        from.startActivity(intent);
    }

    private static final String TAG = "MainActivity";

    private ActivityMainBinding mBinding;
    private List<Fragment> mFragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initView();
        initListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ADD_COLD_ACCOUNT_REQUEST_CODE) {
            String address = data.getStringExtra("address");
            String publicKey = data.getStringExtra("publicKey");
            Account account = new Account();
            account.setAddress(address);
            account.setPublicKey(publicKey);
            mWallet.getColdAccounts().add(account);
            SPUtils.setString(Constants.MONITOR, JSON.toJSONString(mWallet.getColdAccounts()));
            AppBus.getInstance().post(new AppEvent(AppEventType.COLD_ADD));
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void initView() {
        mFragments.add(HomeWalletFragment.newInstance());
        mFragments.add(SettingFragment.newInstance());
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        };
        mBinding.vpMain.setAdapter(adapter);
        TabLayout tabLayout = mBinding.tlMain;
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.getTabAt(0).setCustomView(getTab(R.string.nav_wallet, R.drawable.tab_wallet));
        tabLayout.getTabAt(1).setCustomView(getTab(R.string.nav_settings, R.drawable.tab_setting));
    }

    private void initListener() {
        mBinding.tlMain.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mBinding.vpMain.setCurrentItem(tab.getPosition());
                int pos = tab.getPosition();
                for (int i = 0; i < mBinding.tlMain.getTabCount(); i++) {
                    View view = mBinding.tlMain.getTabAt(i).getCustomView();
                    TextView textView = view.findViewById(R.id.tv_text);
                    if (pos == i) {
                        textView.setTextColor(ContextCompat.getColor(mActivity, R.color.text_strong));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(mActivity, R.color.text_weak));
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mBinding.vpMain.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mBinding.tlMain.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private View getTab(@StringRes int name, int iconID) {
        View tabView = LayoutInflater.from(this).inflate(R.layout.item_tab, null);
        TextView tv = tabView.findViewById(R.id.tv_text);
        tv.setText(name);
        ImageView im = tabView.findViewById(R.id.iv_icon);
        im.setImageResource(iconID);
        return tabView;
    }

    public void changeLanguage() {
        changeAppLanguage();
        mApp.setLanguageChange(true);
        recreate();
    }

    private void changeAppLanguage() {
        Resources resources = getResources();
        int languageType = SPUtils.getInt(Constants.LANGUAGE, Constants.LAN_EN_US);
        Configuration config = resources.getConfiguration();
        if (languageType == Constants.LAN_EN_US) {
            config.locale = Locale.ENGLISH;
        } else {
            config.locale = Locale.SIMPLIFIED_CHINESE;
        }
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(config, dm);
    }
}
