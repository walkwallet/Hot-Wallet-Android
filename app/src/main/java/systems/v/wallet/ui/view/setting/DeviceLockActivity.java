package systems.v.wallet.ui.view.setting;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.databinding.ActivityDeviceLockBinding;
import systems.v.wallet.databinding.DialogBottomSelectBinding;
import systems.v.wallet.ui.BaseActivity;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.FingerprintManagerUtil;
import systems.v.wallet.utils.SPUtils;


public class DeviceLockActivity extends BaseActivity implements View.OnClickListener {
    public static void launch(Activity from) {
        Intent intent = new Intent(from, DeviceLockActivity.class);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private ActivityDeviceLockBinding mBinding;
    private DialogBottomSelectBinding mSelectBinding;
    private Dialog mSelectDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_device_lock);
        mSelectBinding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.dialog_bottom_select, null, false);
        setAppBar(mBinding.toolbar);
        mBinding.setClick(this);
        mSelectBinding.setClick(this);
        if (FingerprintManagerUtil.isAvailable(this)) {
            mBinding.flFingerprint.setVisibility(View.VISIBLE);
        } else {
            mBinding.flFingerprint.setVisibility(View.GONE);
        }
        int autoLockTime = SPUtils.getInt(Constants.AUTO_LOCK, Constants.AUTOLOCK_CLOSE);
        switch (autoLockTime) {
            case Constants.AUTOLOCK_5:
                mBinding.ciAuotoLock.setRightText(R.string.setting_5_min);
                break;
            case Constants.AUTOLOCK_10:
                mBinding.ciAuotoLock.setRightText(R.string.setting_10_min);
                break;
            case Constants.AUTOLOCK_CLOSE:
                mBinding.ciAuotoLock.setRightText(R.string.setting_auto_lock_close);
                break;
        }
        mBinding.switchFingerprint.setChecked(SPUtils.getBoolean(Constants.FINGERPRINT, true));
        initListener();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_top:
                SPUtils.setInt(Constants.AUTO_LOCK, Constants.AUTOLOCK_5);
                mBinding.ciAuotoLock.setRightText(R.string.setting_5_min);
                mSelectBinding.tvTop.setTextColor(ContextCompat.getColor(this, R.color.text_strong));
                mSelectBinding.tvBottom.setTextColor(ContextCompat.getColor(this, R.color.color_orange_strong));
                if (mSelectDialog.isShowing()) {
                    mSelectDialog.dismiss();
                }
                break;
            case R.id.tv_bottom:
                SPUtils.setInt(Constants.AUTO_LOCK, Constants.AUTOLOCK_10);
                mBinding.ciAuotoLock.setRightText(R.string.setting_10_min);
                mSelectBinding.tvTop.setTextColor(ContextCompat.getColor(this, R.color.color_orange_strong));
                mSelectBinding.tvBottom.setTextColor(ContextCompat.getColor(this, R.color.text_strong));
                if (mSelectDialog.isShowing()) {
                    mSelectDialog.dismiss();
                }
                break;
            case R.id.tv_third:
                SPUtils.setInt(Constants.AUTO_LOCK, Constants.AUTOLOCK_CLOSE);
                mBinding.ciAuotoLock.setRightText(R.string.setting_auto_lock_close);
                mSelectBinding.tvTop.setTextColor(ContextCompat.getColor(this, R.color.color_orange_strong));
                mSelectBinding.tvBottom.setTextColor(ContextCompat.getColor(this, R.color.text_strong));
                if (mSelectDialog.isShowing()) {
                    mSelectDialog.dismiss();
                }
                break;
            case R.id.tv_cancel:
                if (mSelectDialog.isShowing()) {
                    mSelectDialog.dismiss();
                }
                break;
            case R.id.ci_auoto_lock:
                showRedBlueDialog();
                break;
        }
    }

    private void initListener() {
        mBinding.switchFingerprint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtils.setBoolean(Constants.FINGERPRINT, isChecked);
            }
        });
    }

    private void showRedBlueDialog() {
        if (mSelectDialog == null && mSelectBinding != null) {
            showChosen();
            mSelectBinding.tvCancel.setTextColor(ContextCompat.getColor(this, R.color.color_orange_strong));
            mSelectBinding.tvTop.setText(R.string.setting_5_min);
            mSelectBinding.tvBottom.setText(R.string.setting_10_min);
            mSelectBinding.tvThird.setText(R.string.setting_auto_lock_close);
            mSelectBinding.tvForth.setVisibility(View.GONE);
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
        mSelectBinding.tvTop.setTextColor(ContextCompat.getColor(this, R.color.color_orange_strong));
        mSelectBinding.tvBottom.setTextColor(ContextCompat.getColor(this, R.color.color_orange_strong));
        mSelectBinding.tvThird.setTextColor(ContextCompat.getColor(this, R.color.color_orange_strong));
        mSelectBinding.tvCancel.setTextColor(ContextCompat.getColor(this, R.color.color_orange_strong));
        int autoLockTime = SPUtils.getInt(Constants.AUTO_LOCK, Constants.AUTOLOCK_CLOSE);
        switch (autoLockTime) {
            case Constants.AUTOLOCK_5:
                mSelectBinding.tvTop.setTextColor(ContextCompat.getColor(this, R.color.text_strong));
                break;
            case Constants.AUTOLOCK_10:
                mSelectBinding.tvBottom.setTextColor(ContextCompat.getColor(this, R.color.text_strong));
            case Constants.AUTOLOCK_CLOSE:
                mSelectBinding.tvThird.setTextColor(ContextCompat.getColor(this, R.color.text_strong));
                break;
        }
    }
}
