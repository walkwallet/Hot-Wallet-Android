package systems.v.wallet.ui.view.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import java.util.UUID;

import androidx.databinding.DataBindingUtil;
import systems.v.wallet.BuildConfig;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.basic.wallet.Agent;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.databinding.ActivityTipsBinding;
import systems.v.wallet.ui.BaseActivity;
import systems.v.wallet.ui.view.main.MainActivity;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.SPUtils;

public class TipsActivity extends BaseActivity implements View.OnClickListener {

    public static final int TYPE_PASSWORD_WEAK = 0; // password strength level
    public static final int TYPE_SUCCESS = 1;

    public static void launch(Activity from, int type, String seed, String password) {
        Intent intent = new Intent(from, TipsActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("seed", seed);
        intent.putExtra("password", password);
        from.startActivity(intent);
        openAnimVertical(from);
    }

    public static void launch(Activity from, int type, String password) {
        Intent intent = new Intent(from, TipsActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("password", password);
        from.startActivity(intent);
        openAnimVertical(from);
    }

    private ActivityTipsBinding mBinding;
    private AsyncTask mLoadBackupTask = null;
    private int mType = TYPE_PASSWORD_WEAK;
    private String mSeed;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tips);
        mType = getIntent().getIntExtra("type", TYPE_PASSWORD_WEAK);
        mPassword = getIntent().getStringExtra("password");
        mSeed = getIntent().getStringExtra("seed");
        if (mType == TYPE_SUCCESS) {
            if (TextUtils.isEmpty(mSeed)) {
                mSeed = Wallet.generateSeed();
            }
            saveWallet(false);
        }
        initView();
        initListener();
    }

    @Override
    public void finish() {
        super.finish();
        closeAnimVertical(mActivity);
    }

    @Override
    protected void onDestroy() {
        if (mLoadBackupTask != null && mLoadBackupTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadBackupTask.cancel(true);
        }
        super.onDestroy();
    }

    private void initView() {
        if (mType == TYPE_PASSWORD_WEAK) {
            mBinding.ivTips.setImageResource(R.drawable.ico_password_tip_warning);
            mBinding.tvLabel.setText(R.string.wallet_create_password_weak_label);
            mBinding.tvTips.setText(R.string.wallet_create_password_weak_tips);
            mBinding.btnBottomFirst.setText(R.string.wallet_reset_password);
            mBinding.btnBottomSecond.setText(R.string.wallet_continue_create);
        } else if (mType == TYPE_SUCCESS) {
            mBinding.ivTips.setImageResource(R.drawable.ico_success);
            mBinding.tvLabel.setText(R.string.wallet_password_create_success_label);
            mBinding.tvTips.setText(R.string.wallet_password_create_success_tips);
            mBinding.btnBottomFirst.setText(R.string.wallet_backup);
            mBinding.btnBottomSecond.setText(R.string.wallet_backup_later);
        }
    }

    private void initListener() {
        mBinding.btnBottomFirst.setOnClickListener(this);
        mBinding.btnBottomSecond.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bottom_first:
                onBottomFirstClick();
                break;
            case R.id.btn_bottom_second:
                onBottomSecondClick();
                break;
        }
    }

    private void onBottomFirstClick() {
        if (mType == TYPE_PASSWORD_WEAK) {
            finish();
        } else if (mType == TYPE_SUCCESS) {
            GenerateSeedActivity.launch(mActivity, mSeed);
        }
    }

    private void onBottomSecondClick() {
        if (mType == TYPE_PASSWORD_WEAK) {
            // ignore password strength level, continue create
            if (TextUtils.isEmpty(mSeed)) {
                TipsActivity.launch(this, TipsActivity.TYPE_SUCCESS, mSeed, mPassword);
            } else {
                saveWallet(true);
            }
        } else if (mType == TYPE_SUCCESS) {
            MainActivity.launch(mActivity, true);
        }
    }

    /**
     * @param launch if true, will auto launch MainActivity
     */
    private void saveWallet(boolean launch) {
        String network = SPUtils.getString(Constants.NETWORK_ENVIRONMENT);
        Agent agent = new Agent("Walk Wallet Hot", BuildConfig.VERSION_NAME, network);
        Wallet wallet = new Wallet(mSeed, network, agent);
        wallet.setPassword(mPassword);
        wallet.setSalt(UUID.randomUUID().toString());
        wallet.append(1);
        mApp.setWallet(wallet);
        mLoadBackupTask = new LoadBackupTask(launch).execute(wallet);
    }


    private class LoadBackupTask extends AsyncTask<Wallet, Void, Void> {

        private boolean launch;

        public LoadBackupTask(boolean launch) {
            this.launch = launch;
        }

        @Override
        protected Void doInBackground(Wallet... params) {
            Wallet wallet = params[0];
            FileUtil.save(mActivity, wallet);
            return null;
        }

        @Override
        protected void onPreExecute() {
            showLoading();
        }

        @Override
        protected void onPostExecute(Void a) {
            hideLoading();
            if (launch) {
                MainActivity.launch(mActivity, true);
            }
        }
    }
}
