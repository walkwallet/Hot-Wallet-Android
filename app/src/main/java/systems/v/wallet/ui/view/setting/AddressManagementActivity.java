package systems.v.wallet.ui.view.setting;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import systems.v.wallet.R;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.databinding.ActivityAddressManagementBinding;
import systems.v.wallet.entity.AccountEntity;
import systems.v.wallet.ui.BaseActivity;
import systems.v.wallet.ui.view.setting.adapter.AddressManagementAdapter;
import systems.v.wallet.ui.widget.wrapper.BaseAdapter;
import systems.v.wallet.utils.bus.AppEvent;
import systems.v.wallet.utils.bus.annotation.Subscribe;

public class AddressManagementActivity extends BaseActivity {
    public static void launch(Activity from) {
        Intent intent = new Intent(from, AddressManagementActivity.class);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private ActivityAddressManagementBinding mBinding;
    private AddressManagementAdapter mAdapter;
    private List<AccountEntity> mWalletList = new ArrayList<>();
    private List<AccountEntity> mMonitorList = new ArrayList<>();
    private List<AccountEntity> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_address_management);
        setAppBar(mBinding.toolbar);
        mAdapter = new AddressManagementAdapter(mData, mActivity);
        mBinding.rvAddressManagement.setLayoutManager(new LinearLayoutManager(mActivity));
        mBinding.rvAddressManagement.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                if (position >= 0 && position < mData.size()) {
                    Account account = mData.get(position);
                    AddressManagementDetailActivity.launch(mActivity, account.getPublicKey());
                }
            }
        });
        initData();
    }

    @Subscribe
    public void onAppEvent(AppEvent event) {
        switch (event.getType()) {
            case COLD_REMOVE:
                initData();
                break;
        }
    }

    private void initData() {
        if (mWallet != null) {
            mData.clear();
            mWalletList.clear();
            mMonitorList.clear();
            List<Account> tmpAccounts = mWallet.getAccounts();
            for (int i = 0; i < tmpAccounts.size(); i++) {
                mWalletList.add(new AccountEntity(tmpAccounts.get(i)));
            }
            tmpAccounts = mWallet.getColdAccounts();
            for (int i = 0; i < tmpAccounts.size(); i++) {
                mMonitorList.add(new AccountEntity(tmpAccounts.get(i)));
            }
            if (mWalletList.size() > 0) {
                mWalletList.get(0).setHeaderWallet(true);
            }
            if (mMonitorList.size() > 0) {
                mMonitorList.get(0).setHeaderMonitor(true);
            }
            mData.addAll(mWalletList);
            mData.addAll(mMonitorList);
            mAdapter.notifyDataSetChanged();
        }
    }
}
