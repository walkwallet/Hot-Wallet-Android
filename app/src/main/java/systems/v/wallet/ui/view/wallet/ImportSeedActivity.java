package systems.v.wallet.ui.view.wallet;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.wallet.Operation;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.databinding.ActivityImportSeedBinding;
import systems.v.wallet.ui.BaseActivity;
import systems.v.wallet.ui.view.transaction.ScannerActivity;
import systems.v.wallet.utils.UIUtil;

public class ImportSeedActivity extends BaseActivity {
    public static void launch(Activity from) {
        Intent intent = new Intent(from, ImportSeedActivity.class);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private static final String TAG = "ImportSeedActivity";

    private ActivityImportSeedBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_import_seed);
        setAppBar(mBinding.toolbar);
        initView();
        initListener();
    }

    @Override
    public void finish() {
        super.finish();
        closeAnimHorizontal(mActivity);
    }

    private void initView() {
        mBinding.btnNext.setEnabled(false);
    }

    private void initListener() {
        mBinding.etWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String word = s.toString();
                String[] words = word.trim().split(" ");
                if (words.length == 15) {
                    mBinding.btnNext.setEnabled(true);
                } else {
                    mBinding.btnNext.setEnabled(false);
                }
                if (TextUtils.isEmpty(word)) {
                    mBinding.btnNext.setEnabled(false);
                } else {
                    mBinding.btnNext.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBinding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String word = mBinding.etWord.getText().toString();
                if (!Wallet.validateSeedPhrase(word)) {
                    new AlertDialog.Builder(mActivity, R.style.BasicAlertDialog_Light)
                            .setIcon(R.drawable.basic_ico_alert)
                            .setTitle(R.string.not_office_seed_title)
                            .setMessage(R.string.not_office_seed_tips)
                            .setPositiveButton(R.string.text_continue, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SetPasswordActivity.launch(mActivity, word);
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .confirm();
                } else {
                    SetPasswordActivity.launch(mActivity, word);
                }
            }
        });
        mBinding.ivScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScannerActivity.launch(ImportSeedActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String qrContents = result.getContents();
            Log.d(TAG, "scan result is " + qrContents);
            try {
                Operation op = Operation.parse(qrContents);
                if (op == null){
                    mBinding.etWord.setText(qrContents);
                }else if (op.validate(Operation.SEED)) {
                    String seed = op.getString("seed");
                    mBinding.etWord.setText(seed);
                }
            } catch (Exception e) {
                Log.e(TAG, "unsupported seed");
                UIUtil.showUnsupportQrCodeDialog(this);
            }
        }
    }
}
