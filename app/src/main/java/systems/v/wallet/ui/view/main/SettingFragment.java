package systems.v.wallet.ui.view.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.databinding.DialogBottomSelectBinding;
import systems.v.wallet.databinding.FragmentSettingBinding;
import systems.v.wallet.ui.BaseFragment;
import systems.v.wallet.ui.view.setting.AboutUsActivity;
import systems.v.wallet.ui.view.setting.AddressManagementActivity;
import systems.v.wallet.ui.view.setting.DeviceLockActivity;
import systems.v.wallet.ui.view.wallet.GenerateSeedActivity;
import systems.v.wallet.ui.view.wallet.WalletInitActivity;
import systems.v.wallet.ui.widget.VerifyDialogFragment;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.SPUtils;

public class SettingFragment extends BaseFragment implements View.OnClickListener {

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    private FragmentSettingBinding mBinding;
    private DialogBottomSelectBinding mSelectBinding;
    private Dialog mSelectDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, null, false);
        mBinding.setClick(this);
        mSelectBinding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.dialog_bottom_select, null, false);
        mSelectBinding.setClick(this);
        int lan = SPUtils.getInt(Constants.LANGUAGE, Constants.LAN_EN_US);
        if (lan == Constants.LAN_EN_US) {
            mBinding.ciLanguage.setRightText(R.string.setting_lan_en);
        } else if (lan == Constants.LAN_ZH_CN){
            mBinding.ciLanguage.setRightText(R.string.setting_lan_cn);
        } else if (lan == Constants.LAN_ZH_TW){
            mBinding.ciLanguage.setRightText(R.string.setting_lan_tw);
        } else if (lan == Constants.LAN_KO){
            mBinding.ciLanguage.setRightText(R.string.setting_lan_ko);
        }
        if (App.getInstance().getWallet() != null) {
            if (App.getInstance().getWallet().getNetwork().equals(Wallet.TEST_NET)) {
                mBinding.ciNetwork.setRightText(getString(R.string.network_test_net));
            } else {
                mBinding.ciNetwork.setRightText(getString(R.string.network_main_net));
            }
        }
        return mBinding.getRoot();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ci_device_lock:
                DeviceLockActivity.launch(mActivity);
                break;
            case R.id.ci_about_us:
                AboutUsActivity.launch(mActivity);
                break;
            case R.id.ci_address:
                AddressManagementActivity.launch(mActivity);
                break;
            case R.id.ci_back_up:
                new VerifyDialogFragment.Builder((AppCompatActivity) mActivity)
                        .setTitle(getString(R.string.word_backup_word))
                        .setOnNextListener(new VerifyDialogFragment.OnNextListener() {
                            @Override
                            public void onInput(String password) {

                            }

                            @Override
                            public void onNext() {
                                GenerateSeedActivity.launch(mActivity, App.getInstance().getWallet().getSeed());
                            }
                        }).show();
                break;
            case R.id.ci_sign_out:
                new AlertDialog.Builder(mActivity)
                        .setIcon(R.drawable.basic_ico_alert)
                        .setTitle(R.string.log_out_wallet_title)
                        .setMessage(R.string.log_out_wallet_tips)
                        .setPositiveButton(R.string.setting_logout, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SPUtils.clear();
                                FileUtil.deleteWallet(mActivity);
                                App.getInstance().setWallet(null);
                                WalletInitActivity.launch(mActivity);
                                App.getInstance().finishAllActivities();
                            }
                        }).confirm();
                break;
            case R.id.tv_top:
                if (SPUtils.getInt(Constants.LANGUAGE) == Constants.LAN_EN_US) {
                    if (mSelectDialog.isShowing()) {
                        mSelectDialog.dismiss();
                    }
                    return;
                }
                SPUtils.setInt(Constants.LANGUAGE, Constants.LAN_EN_US);
                mBinding.ciLanguage.setRightText(R.string.setting_lan_en);
                if (mSelectDialog.isShowing()) {
                    mSelectDialog.dismiss();
                }
                if (mActivity instanceof MainActivity) {
                    ((MainActivity) mActivity).changeLanguage();
                }
                break;
            case R.id.tv_bottom:
                if (SPUtils.getInt(Constants.LANGUAGE) == Constants.LAN_ZH_CN) {
                    if (mSelectDialog.isShowing()) {
                        mSelectDialog.dismiss();
                    }
                    return;
                }
                SPUtils.setInt(Constants.LANGUAGE, Constants.LAN_ZH_CN);
                mBinding.ciLanguage.setRightText(R.string.setting_lan_cn);
                if (mSelectDialog.isShowing()) {
                    mSelectDialog.dismiss();
                }
                if (mActivity instanceof MainActivity) {
                    ((MainActivity) mActivity).changeLanguage();
                }
                break;
            case R.id.tv_third:
                if (SPUtils.getInt(Constants.LANGUAGE) == Constants.LAN_ZH_TW) {
                    if (mSelectDialog.isShowing()) {
                        mSelectDialog.dismiss();
                    }
                    return;
                }
                SPUtils.setInt(Constants.LANGUAGE, Constants.LAN_ZH_TW);
                mBinding.ciLanguage.setRightText(R.string.setting_lan_tw);
                if (mSelectDialog.isShowing()) {
                    mSelectDialog.dismiss();
                }
                if (mActivity instanceof MainActivity) {
                    ((MainActivity) mActivity).changeLanguage();
                }
                break;
            case R.id.tv_forth:
                if (SPUtils.getInt(Constants.LANGUAGE) == Constants.LAN_KO) {
                    if (mSelectDialog.isShowing()) {
                        mSelectDialog.dismiss();
                    }
                    return;
                }
                SPUtils.setInt(Constants.LANGUAGE, Constants.LAN_KO);
                mBinding.ciLanguage.setRightText(R.string.setting_lan_ko);
                if (mSelectDialog.isShowing()) {
                    mSelectDialog.dismiss();
                }
                if (mActivity instanceof MainActivity) {
                    ((MainActivity) mActivity).changeLanguage();
                }
                break;
            case R.id.tv_cancel:
                if (mSelectDialog.isShowing()) {
                    mSelectDialog.dismiss();
                }
                break;
            case R.id.ci_language:
                showRedBlueDialog();
                break;
        }
    }

    private void showRedBlueDialog() {
        if (mSelectDialog == null && mSelectBinding != null) {
            showChosen();
            mSelectBinding.tvCancel.setTextColor(ContextCompat.getColor(mActivity, R.color.color_orange_strong));
            mSelectBinding.tvTop.setText(R.string.setting_lan_en);
            mSelectBinding.tvBottom.setText(R.string.setting_lan_cn);
            mSelectBinding.tvThird.setText(R.string.setting_lan_tw);
            mSelectBinding.tvForth.setText(R.string.setting_lan_ko);
            mSelectDialog = new Dialog(mActivity, R.style.BottomDialog);
            mSelectDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            if (mSelectBinding.getRoot().getParent() != null) {
                ((ViewGroup) mSelectBinding.getRoot().getParent()).removeView(mSelectBinding.getRoot());
            }
            mSelectDialog.setContentView(mSelectBinding.getRoot());
            mSelectDialog.setCancelable(true);
            Window window = mSelectDialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.AnimBottom);
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
        mSelectDialog.show();
    }

    private void showChosen() {
        if (mSelectBinding == null) {
            return;
        }
        mSelectBinding.tvTop.setTextColor(getResources().getColor(R.color.color_orange_strong));
        mSelectBinding.tvBottom.setTextColor(getResources().getColor(R.color.color_orange_strong));
        mSelectBinding.tvCancel.setTextColor(getResources().getColor(R.color.color_orange_strong));
        mSelectBinding.tvThird.setTextColor(getResources().getColor(R.color.color_orange_strong));
        mSelectBinding.tvForth.setTextColor(getResources().getColor(R.color.color_orange_strong));
        int lan = SPUtils.getInt(Constants.LANGUAGE, Constants.LAN_EN_US);
        switch (lan) {
            case Constants.LAN_EN_US:
                mSelectBinding.tvTop.setTextColor(getResources().getColor(R.color.text_strong));
                break;
            case Constants.LAN_ZH_CN:
                mSelectBinding.tvBottom.setTextColor(getResources().getColor(R.color.text_strong));
                break;
            case Constants.LAN_ZH_TW:
                mSelectBinding.tvThird.setTextColor(getResources().getColor(R.color.text_strong));
                break;
            case Constants.LAN_KO:
                mSelectBinding.tvForth.setTextColor(getResources().getColor(R.color.text_strong));
                break;
        }
    }
}
