package systems.v.wallet.ui.view.setting;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.alibaba.fastjson.JSON;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.databinding.ActivityAddressManagementDetailBinding;
import systems.v.wallet.ui.BaseActivity;
import systems.v.wallet.ui.widget.VerifyDialogFragment;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.SPUtils;
import systems.v.wallet.utils.UIUtil;
import systems.v.wallet.utils.bus.AppBus;
import systems.v.wallet.utils.bus.AppEvent;
import systems.v.wallet.utils.bus.AppEventType;

public class AddressManagementDetailActivity extends BaseActivity implements View.OnClickListener {

    public static void launch(Activity from, String publicKey) {
        Intent intent = new Intent(from, AddressManagementDetailActivity.class);
        intent.putExtra("publicKey", publicKey);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private ActivityAddressManagementDetailBinding mBinding;
    private Account mAccount;
    private boolean mIsShowPrivateKey = false;
    private boolean mIsShowSeed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_address_management_detail);
        mBinding.setClick(this);
        setAppBar(mBinding.toolbar);
        String publicKey = getIntent().getStringExtra("publicKey");
        mAccount = App.getInstance().getWallet().getAccount(publicKey);
        if (mAccount == null) {
            return;
        }
        initView();
    }

    private void initView() {
        if (mAccount.isColdAccount()) {
            mBinding.llMonitor.setVisibility(View.GONE);
            mBinding.tvDelete.setVisibility(View.VISIBLE);
        } else {
            mBinding.llMonitor.setVisibility(View.VISIBLE);
            mBinding.tvDelete.setVisibility(View.GONE);
        }
        mBinding.tvAddress.setText(mAccount.getAddress());
        mBinding.tvBalance.setText(CoinUtil.formatWithUnit(mAccount.getAvailable()));
        mBinding.tvPublicKey.setText(mAccount.getPublicKey());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_private_key:
                if (mIsShowPrivateKey) {
                    UIUtil.copyToClipboard(mActivity, mAccount.getPrivateKey());
                    return;
                }
                new VerifyDialogFragment.Builder((AppCompatActivity) mActivity)
                        .setTitle(R.string.setting_show_private_key)
                        .setOnNextListener(
                                new VerifyDialogFragment.OnNextListener() {
                                    @Override
                                    public void onInput(String password) {

                                    }

                                    @Override
                                    public void onNext() {
                                        mBinding.tvPrivateKey.setText(mAccount.getPrivateKey());
                                        mIsShowPrivateKey = true;
                                    }
                                }).show();
                break;
            case R.id.ll_seed:
                if (mIsShowSeed) {
                    UIUtil.copyToClipboard(mActivity, mAccount.getSeed());
                    return;
                }
                new VerifyDialogFragment.Builder((AppCompatActivity) mActivity)
                        .setTitle(R.string.setting_show_seed)
                        .setOnNextListener(
                                new VerifyDialogFragment.OnNextListener() {
                                    @Override
                                    public void onInput(String password) {

                                    }

                                    @Override
                                    public void onNext() {
                                        mBinding.tvSeed.setText(mAccount.getSeed());
                                        mIsShowSeed = true;
                                    }
                                }).show();
                break;
            case R.id.tv_delete:
                new AlertDialog.Builder(mActivity)
                        .setIcon(R.drawable.basic_ico_alert)
                        .setMessage(R.string.setting_delete_monitor_tips)
                        .setPositiveButton(R.string.setting_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<Account> coldList = mWallet.getColdAccounts();
                                for (int i = coldList.size() - 1; i >= 0; i--) {
                                    if (coldList.get(i).getAddress().equals(mAccount.getAddress())) {
                                        coldList.remove(i);
                                    }
                                }
                                SPUtils.setString(Constants.MONITOR, JSON.toJSONString(coldList));
                                AppBus.getInstance().post(new AppEvent(AppEventType.COLD_REMOVE));
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).confirm();
                break;
            case R.id.ll_address:
                UIUtil.copyToClipboard(mActivity, mAccount.getAddress());
                break;
            case R.id.ll_public_key:
                UIUtil.copyToClipboard(mActivity, mAccount.getPublicKey());
                break;
        }
    }
}
