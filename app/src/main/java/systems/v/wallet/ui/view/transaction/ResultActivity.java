package systems.v.wallet.ui.view.transaction;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.JsonUtil;
import systems.v.wallet.basic.wallet.Operation;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.api.NodeAPI;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.databinding.ActivityResultBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.utils.UIUtil;

public class ResultActivity extends BaseThemedActivity {

    public static void launch(Activity from, String publicKey, Transaction tx) {
        Intent intent = new Intent(from, ResultActivity.class);
        intent.putExtra("tx", JSON.toJSONString(tx));
        intent.putExtra("publicKey", publicKey);
        from.startActivity(intent);
        openAlpha(from);
    }

    private static final String TAG = "ResultActivity";

    private ActivityResultBinding mBinding;
    private Transaction mTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTransaction = JSON.parseObject(getIntent().getStringExtra("tx"), Transaction.class);
        showReviewFragment();
    }

    @Override
    protected void setActivityTheme(boolean isColdAccount) {
        if (isColdAccount) {
            setTheme(R.style.AppTheme_Transparent_Blue);
        } else {
            setTheme(R.style.AppTheme_Transparent_Orange);
        }
    }

    private void setContent() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof DialogFragment) {
                ((DialogFragment) fragment).dismiss();
            }
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_result);
        mBinding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mApp.popActivity(2);
            }
        });
        int textId = 0;
        int type = mTransaction.getTransactionType();
        switch (type) {
            case Transaction.PAYMENT:
                textId = R.string.send_payment_success;
                break;
            case Transaction.LEASE:
                textId = R.string.send_lease_success;
                break;
            case Transaction.CANCEL_LEASE:
                textId = R.string.send_cancel_lease_success;
                break;
            case Transaction.ContractRegister:
                textId = R.string.create_token_success;
                break;
            case Transaction.ContractExecute:
                textId = R.string.create_token_success;
                break;
        }
        if (textId != 0) {
            mBinding.tvInfo.setText(getString(textId,
                    CoinUtil.formatWithUnit(mTransaction.getAmount())));
        }
        mBinding.tvAddress.setText(mTransaction.getRecipient());
    }

    @Override
    public void finish() {
        super.finish();
        closeAlpha(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String qrContents = result.getContents();
            if (!TextUtils.isEmpty(qrContents)) {
                if (JsonUtil.isJsonString(qrContents)) {
                    // scan signature
                    Operation op = Operation.parse(qrContents);
                    if (op == null || !op.validate(Operation.SIGNATURE)) {
                        Log.e(TAG, "scan result is not an signature");
                        UIUtil.showUnsupportQrCodeDialog(this);
                        return;
                    }
                    String signature = op.getString("signature");
                    if (!TextUtils.isEmpty(signature)) {
                        // review again
                        mTransaction.setSignature(signature);
                        showReviewAgainFragment();
                    }
                }
            }
        }
    }

    private void showReviewFragment() {
        int buttonTextId = mAccount.isColdAccount() ?
                R.string.send_generate_qr_code_btn : R.string.confirm;
        new ReviewFragment.Builder(this)
                .setButtonText(buttonTextId)
                .setOnNextListener(new TransactionDialogFragment.OnNextListener() {
                    @Override
                    public void onNext() {
                        if (mAccount.isColdAccount()) {
                            showQrCodeFragment();
                        } else {
                            showVerifyPasswordFragment();
                        }
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (mBinding == null) {
                            finish();
                        }
                    }
                })
                .show(mAccount.getPublicKey(), mTransaction);
    }

    private void showQrCodeFragment() {
        String txString = mTransaction.toTxString();
        Log.d(TAG, "tx is " + txString);
        QrCodeFragment fragment = QrCodeFragment.newInstance(mAccount.getPublicKey(), txString);
        fragment.show(getSupportFragmentManager(), "qrcode");
        fragment.setOnNextListener(new TransactionDialogFragment.OnNextListener() {
            @Override
            public void onNext() {
                // scan signature
                ScannerActivity.launch(mActivity);
            }
        });
    }

    private void showVerifyPasswordFragment() {
        VerifyPasswordFragment.show(this, mAccount.getPublicKey(), new TransactionDialogFragment.OnNextListener() {
            @Override
            public void onNext() {
                mTransaction.sign(mAccount);
                send();
            }
        });
    }

    private void showReviewAgainFragment() {
        new ReviewAgainFragment.Builder(this)
                .setButtonText(R.string.send)
                .setOnNextListener(new TransactionDialogFragment.OnNextListener() {
                    @Override
                    public void onNext() {
                        send();
                    }
                })
                .show(mAccount.getPublicKey(), mTransaction);
    }

    private void send() {
        Observable<RespBean> observable = null;
        NodeAPI api = RetrofitHelper.getInstance().getNodeAPI();
        int type = mTransaction.getTransactionType();
        switch (type) {
            case Transaction.PAYMENT:
                observable = api.payment(mTransaction.toRequestBody());
                break;
            case Transaction.LEASE:
                observable = api.lease(mTransaction.toRequestBody());
                break;
            case Transaction.CANCEL_LEASE:
                observable = api.cancelLease(mTransaction.toRequestBody());
                break;
            case Transaction.ContractRegister:
                observable = api.registerContract(mTransaction.toRequestBody());
                break;
            case Transaction.ContractExecute:
                observable = api.executeContract(mTransaction.toRequestBody());
                break;
        }
        if (observable == null) {
            return;
        }
        Disposable d = observable.compose(this.<RespBean>bindLoading())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RespBean>() {
                    @Override
                    public void accept(RespBean resp) throws Exception {
                        if (resp.getCode() == 0) {
                            setContent();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }
}
