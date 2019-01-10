package systems.v.wallet.ui.view.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.basic.wallet.Operation;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.databinding.ActivityAddColdAccountBinding;
import systems.v.wallet.ui.BaseActivity;
import systems.v.wallet.ui.view.transaction.ScannerActivity;
import systems.v.wallet.utils.UIUtil;

public class AddColdAccountActivity extends BaseActivity {

    public static void launch(Activity from, int requestCode) {
        Intent intent = new Intent(from, AddColdAccountActivity.class);
        from.startActivityForResult(intent, requestCode);
    }

    private static final String TAG = "AddColdAccountActivity";

    private ActivityAddColdAccountBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_cold_account);
        setAppBar(mBinding.toolbar);
        mBinding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = mBinding.etAddress.getText().toString();
                String publicKey = mBinding.etPublicKey.getText().toString();
                if (validate(address, publicKey)) {
                    Intent intent = new Intent();
                    intent.putExtra("address", address);
                    intent.putExtra("publicKey", publicKey);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
        ScannerActivity.launch(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                ScannerActivity.launch(this);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            finish();
            return;
        }
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String qrContents = result.getContents();
            Log.d(TAG, "scan result is " + qrContents);
            Operation op = Operation.parse(qrContents);
            if (op == null || !op.validate(Operation.ACCOUNT)) {
                Log.e(TAG, "scan result is not an account");
                UIUtil.showUnsupportQrCodeDialog(this);
                return;
            }
            String address = op.getString("address");
            String publicKey = op.getString("publicKey");
            if (validate(address, publicKey)) {
                if (mWallet.getAccount(publicKey) != null) {
                    UIUtil.showInfo(mActivity, R.string.add_cold_account_address_exist_error);
                    return;
                }
                mBinding.etAddress.setText(address);
                mBinding.etPublicKey.setText(publicKey);
            } else {
                UIUtil.showUnsupportQrCodeDialog(mActivity);
            }
        }
    }

    private boolean validate(String address, String publicKey) {
        if (TextUtils.isEmpty(address) || TextUtils.isEmpty(publicKey)) {
            return false;
        }
        if (!Wallet.validateAddress(address)) {
            return false;
        }
        return Wallet.getAddress(mWallet.getNetwork(), publicKey).equals(address);
    }
}
