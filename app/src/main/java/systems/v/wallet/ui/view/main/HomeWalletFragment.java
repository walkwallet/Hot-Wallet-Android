package systems.v.wallet.ui.view.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import systems.v.wallet.R;
import systems.v.wallet.databinding.FragmentHomeWalletBinding;
import systems.v.wallet.ui.BaseFragment;

public class HomeWalletFragment extends BaseFragment {

    public static HomeWalletFragment newInstance() {
        return new HomeWalletFragment();
    }

    private FragmentHomeWalletBinding mBinding;
    private List<String> mTabs = new ArrayList<>();
    private List<TabWalletFragment> mFragments = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_wallet, null, false);
        initView();
        return mBinding.getRoot();
    }

    private void initView() {
        TabWalletFragment walletFragment = TabWalletFragment.newInstance(TabWalletFragment.TYPE_WALLET);
        TabWalletFragment monitorFragment = TabWalletFragment.newInstance(TabWalletFragment.TYPE_MONITOR);
        mFragments.add(walletFragment);
        mFragments.add(monitorFragment);
        mTabs.add(getString(R.string.detail_wallet_title));
        mTabs.add(getString(R.string.detail_monitor_title));
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTabs.get(position);
            }
        };
        mBinding.vpWallet.setAdapter(adapter);
        for (int i = 0, len = mTabs.size(); i < len; i++) {
            mBinding.tlWallet.addTab(mBinding.tlWallet.newTab());
        }
        mBinding.tlWallet.setupWithViewPager(mBinding.vpWallet);
        for (int i = 0, len = mTabs.size(); i < len; i++) {
            mBinding.tlWallet.getTabAt(i).setText(mTabs.get(i));
        }
        mBinding.tlWallet.setColorChangeable(true);
        mBinding.tlWallet.setColor(ContextCompat.getColor(mActivity, R.color.orange),
                ContextCompat.getColor(mActivity, R.color.blue));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mBinding != null) {
            if (mBinding.tlWallet.getSelectedTabPosition() == 0) {
                mFragments.get(0).getData();
            } else {
                mFragments.get(1).getData();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBinding != null) {
            if (mBinding.tlWallet.getSelectedTabPosition() == 0) {
                mFragments.get(0).getData();

            } else {
                mFragments.get(1).getData();
            }
        }
    }
}
