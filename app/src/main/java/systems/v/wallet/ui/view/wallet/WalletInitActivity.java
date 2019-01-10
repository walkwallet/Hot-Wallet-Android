package systems.v.wallet.ui.view.wallet;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.databinding.ActivityWalletInitBinding;
import systems.v.wallet.ui.BaseActivity;

public class WalletInitActivity extends BaseActivity implements View.OnClickListener {

    public static void launch(Activity from) {
        Intent intent = new Intent(from, WalletInitActivity.class);
        from.startActivity(intent);
        openAlpha(from);
    }

    private ActivityWalletInitBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_init);
        initListener();
        showWarningDialog();
    }

    @Override
    public void finish() {
        super.finish();
        closeAlpha(mActivity);
    }

    private void initListener() {
        mBinding.btnCreate.setOnClickListener(this);
        mBinding.btnImport.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_import:
                // import wallet
                NetworkActivity.launch(mActivity, NetworkActivity.TYPE_IMPORT);
                break;
            case R.id.btn_create:
                // create wallet
                NetworkActivity.launch(mActivity, NetworkActivity.TYPE_CREATE);
                break;
        }
    }


    private void showWarningDialog() {
        new AlertDialog.Builder(mActivity)
                .setIcon(R.drawable.basic_ico_alert)
                .setTitle(R.string.official_warning)
                .setMessage(R.string.wallet_init_official_warning_dialog_msg)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBinding.llWalletInitContainer.setVisibility(View.VISIBLE);
                    }
                })
                .alert();
    }
}
