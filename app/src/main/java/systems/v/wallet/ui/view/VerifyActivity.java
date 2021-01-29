package systems.v.wallet.ui.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.databinding.DataBindingUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.basic.utils.KeyboardUtil;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.databinding.ActivityVerifyBinding;
import systems.v.wallet.ui.BaseActivity;
import systems.v.wallet.ui.view.main.MainActivity;
import systems.v.wallet.ui.view.wallet.WalletInitActivity;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.FingerprintManagerUtil;
import systems.v.wallet.utils.SPUtils;

public class VerifyActivity extends BaseActivity {

    public static void launch(Activity from) {
        Intent intent = new Intent(from, VerifyActivity.class);
        from.startActivity(intent);
        openAlpha(from);
    }

    public static void launch(Context from) {
        Intent intent = new Intent(from, VerifyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        from.startActivity(intent);
    }

    private static final String TAG = "VerifyActivity";

    private ActivityVerifyBinding mBinding;
    private Dialog mVerifyDialog;
    private int ErrPwdCount = 0;
    private AsyncTask mLoadBackupTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_verify);
        AutoUnlockWallet();
        initView();
    }

    private void initView() {
        mBinding.ivEnter.setEnabled(false);
        if (mWallet != null) {
            if (FingerprintManagerUtil.isAvailable(this)
                    && SPUtils.getBoolean(Constants.FINGERPRINT, true)) {
                mBinding.ivFinger.setVisibility(View.VISIBLE);
                startVerification();
            } else {
                mBinding.ivFinger.setVisibility(View.GONE);
            }
        } else {
            mBinding.ivFinger.setVisibility(View.GONE);
        }
        mBinding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    mBinding.ivEnter.setImageResource(R.drawable.ico_enter_activited);
                    mBinding.ivEnter.setEnabled(true);
                } else {
                    mBinding.ivEnter.setImageResource(R.drawable.ico_enter_normal);
                    mBinding.ivEnter.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBinding.ivFinger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVerification();
            }
        });
        mBinding.ivEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = mBinding.etPassword.getText().toString();
                if (mWallet == null) {
                    mLoadBackupTask = new LoadBackupTask().execute(password);
                } else {
                    if (password.equals(mWallet.getPassword())) {
                        finish();
                        return;
                    }
                    mBinding.etPassword.setText("");
                    showToast(R.string.password_wrong);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void startVerification() {
        FingerprintManagerUtil.startFingerprinterVerification(this,
                new FingerprintManagerUtil.FingerprintListenerAdapter() {

                    @Override
                    public void onAuthenticationStart() {
                        if (mVerifyDialog == null) {
                            mVerifyDialog = new AlertDialog.Builder(mActivity)
                                    .setTitle(R.string.fingerprint_title)
                                    .setMessage(R.string.fingerprint_tips)
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            FingerprintManagerUtil.cancel();
                                            dialog.dismiss();
                                        }
                                    }).create();
                        }
                        mVerifyDialog.show();
                    }

                    @Override
                    public void onNonsupport() {
                        Log.i(TAG, "onNonsupport");
                        showToast(R.string.fingerprint_disable);
                    }

                    @Override
                    public void onEnrollFailed() {
                        Log.i(TAG, "onEnrollFailed");
                        showToast(R.string.fingerprint_no_data);
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                        mVerifyDialog.dismiss();
                        Log.i(TAG, "onAuthenticationSucceeded result = [" + result + "]");
                        showToast(R.string.fingerprint_success);
                        finish();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        Log.i(TAG, "onAuthenticationFailed");
                        showToast(R.string.fingerprint_faild);
                    }

                    @Override
                    public void onAuthenticationError(int errMsgId, CharSequence errString) {
                        Log.i(TAG, "onAuthenticationError errMsgId = [" + errMsgId + "], errString = [" + errString + "]");
                        showToast(errString.toString());
                    }

                    @Override
                    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                        Log.i(TAG, "onAuthenticationHelp helpMsgId = [" + helpMsgId + "], helpString = [" + helpString + "]");
                        showToast(helpString.toString());
                    }
                });
    }

    @Override
    public void finish() {
        super.finish();
        closeAlpha(mActivity);
    }

    @Override
    protected void onDestroy() {
        if (mLoadBackupTask != null && mLoadBackupTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadBackupTask.cancel(true);
        }
        super.onDestroy();
    }

    private void showKeyboard() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                KeyboardUtil.show(mActivity, mBinding.etPassword);
            }
        }, 50);
    }

    private void showPasswordErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.BasicAlertDialog_Light);
        builder.setTitle(R.string.password_wrong)
                .setIcon(R.drawable.basic_ico_alert)
                .setPositiveButton(R.string.continue_input, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBinding.etPassword.setText(null);
                        showKeyboard();
                    }
                });
        ErrPwdCount += 1;
        if (ErrPwdCount > 4) {
            builder.setNegativeButton(R.string.setting_logout, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showLogoutWarningDialog();
                }
            });
        }
        builder.confirm();

    }

    private void showLogoutWarningDialog() {
        new AlertDialog.Builder(mActivity)
                .setIcon(R.drawable.basic_ico_alert)
                .setTitle(R.string.log_out_wallet_title)
                .setMessage(R.string.log_out_wallet_tips)
                .setPositiveButton(R.string.setting_logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SPUtils.clear();
                        FileUtil.deleteWallet(mActivity);
                        mApp.setWallet(null);
                        WalletInitActivity.launch(mActivity);
                        mApp.finishAllActivities();
                    }
                }).confirm();
    }

    private void AutoUnlockWallet() {
        Wallet wallet = App.getInstance().getWallet();
        if (wallet != null && SPUtils.getInt(Constants.AUTO_LOCK, Constants.AUTOLOCK_CLOSE) == -1) {
            finish();
            return;
        }
    }

    private class LoadBackupTask extends AsyncTask<String, Void, Wallet> {

        @Override
        protected Wallet doInBackground(String... params) {
            String password = params[0];
            return FileUtil.load(mApp, password);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }

        @Override
        protected void onPostExecute(Wallet wallet) {
            hideLoading();
            if (wallet != null) {
                List<Account> colds = new ArrayList<>();
                String cold = SPUtils.getString(Constants.MONITOR);
                if (!TextUtils.isEmpty(cold)) {
                    colds = JSONArray.parseArray(cold, Account.class);
                }
                ArrayList<Account> coldList = new ArrayList<>(colds);
                ArrayList<Account> hotsList = wallet.generateAccounts();

                String aliasStr = SPUtils.getString(Constants.ALIAS);
                if(!aliasStr.isEmpty()) {
                    Map<String, String> aliases = JSON.parseObject(aliasStr, Map.class);
                    if (aliases != null) {
                        for (int i = 0; i < coldList.size(); i++) {
                            String a = aliases.get(coldList.get(i).getAddress());
                            if (a != null && !a.isEmpty()) {
                                coldList.get(i).setAlias(a);
                            }
                        }

                        for (int i = 0; i < hotsList.size(); i++) {
                            String a = aliases.get(hotsList.get(i).getAddress());
                            if (a != null && !a.isEmpty()) {
                                hotsList.get(i).setAlias(a);
                            }
                        }
                    }
                }
                wallet.setColdAccounts(coldList);
                wallet.setAccounts(hotsList);

                mApp.setWallet(wallet);
                MainActivity.launch(mActivity, false);
                mApp.finishAllActivities();
            } else {
                showPasswordErrorDialog();
            }
        }
    }
}
