package systems.v.wallet.ui.view.transaction;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.JsonUtil;
import systems.v.wallet.basic.utils.TxUtil;
import systems.v.wallet.basic.wallet.Operation;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.databinding.ActivitySendBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.widget.inputfilter.MaxByteFilter;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.utils.UIUtil;

public class SendActivity extends BaseThemedActivity implements View.OnClickListener {

    public static void launchPayment(Activity from, String publicKey) {
        launch(from, publicKey, Transaction.PAYMENT);
    }

    public static void launchLease(Activity from, String publicKey) {
        launch(from, publicKey, Transaction.LEASE);
    }

    public static void launch(Activity from, String publicKey, int type) {
        Intent intent = new Intent(from, SendActivity.class);
        intent.putExtra("publicKey", publicKey);
        intent.putExtra("type", type);
        from.startActivity(intent);
    }

    private static final String TAG = "SendActivity";

    private ActivitySendBinding mBinding;
    private Transaction mTransaction;
    private int mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_send);
        mType = getIntent().getIntExtra("type", Transaction.PAYMENT);
        setAppBar(mBinding.toolbar);
        mBinding.setClick(this);

        // set balance and fee
        String balance = CoinUtil.formatWithUnit(mAccount.getAvailable());
        mBinding.tvAvailableBalance.setText(getString(R.string.send_available_balance, balance));
        String fee = CoinUtil.formatWithUnit(Transaction.DEFAULT_FEE);
        mBinding.tvFee.setText(fee);

        if (mType == Transaction.LEASE) {
            mBinding.toolbar.setTitle(R.string.send_lease_title);
            mBinding.llAttachment.setVisibility(View.GONE);
            mBinding.tvAmountLabel.setText(R.string.send_lease_amount);
            mBinding.tvSendToLabel.setText(R.string.send_lease_to);
            mBinding.etAddress.setHint(R.string.send_lease_address_input_hint);
        } else {
            mBinding.toolbar.setTitle(R.string.send_payment_title);
            mBinding.flLeaseTips.setVisibility(View.GONE);
            mBinding.tvSendToLabel.setText(R.string.send_payment_to);
            mBinding.etAddress.setHint(R.string.send_address_input_hint);
        }
        mBinding.etAttachment.setFilters(new InputFilter[]{new MaxByteFilter()});
        initListener();
    }

    private void initListener() {
        UIUtil.setAmountInputFilter(mBinding.etAmount);
        mBinding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                long value = CoinUtil.parse(s.toString());
                if (TextUtils.isEmpty(s) || (mAccount.getAvailable() - value) >= Transaction.DEFAULT_FEE) {
                    mBinding.tvBalanceError.setVisibility(View.GONE);
                } else {
                    mBinding.tvBalanceError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBinding.etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s) || Wallet.validateAddress(s.toString())) {
                    mBinding.tvAddressError.setVisibility(View.GONE);
                } else {
                    mBinding.tvAddressError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_max: {
                mBinding.etAmount.setText(CoinUtil.format(mAccount.getAvailable() - Transaction.DEFAULT_FEE));
            }
            break;
            case R.id.btn_paste: {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = cm.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    ClipData.Item item = clipData.getItemAt(0);
                    String text = item.getText().toString();
                    mBinding.etAddress.setText(text);
                }
            }
            break;
            case R.id.btn_scan:
                ScannerActivity.launch(this);
                break;
            case R.id.btn_confirm: {
                String amount = mBinding.etAmount.getText().toString();
                String address = mBinding.etAddress.getText().toString();
                int textId = 0;
                if (TextUtils.isEmpty(amount)) {
                    textId = R.string.send_amount_empty_error;
                } else if ((mAccount.getAvailable() - CoinUtil.parse(amount)) < Transaction.DEFAULT_FEE) {
                    textId = R.string.send_insufficient_balance_error;
                } else if (!Wallet.validateAddress(address)) {
                    textId = R.string.send_address_input_error;
                }
                if (mType == Transaction.LEASE && address.equals(mAccount.getAddress())){
                    textId = R.string.send_lease_to_self_error;
                }
                if (textId != 0) {
                    new AlertDialog.Builder(mActivity)
                            .setMessage(textId)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                    return;
                }
                if (address.equals(mAccount.getAddress())) {
                    ToastUtil.showLongToast(R.string.send_to_self_error);
                }
                generateTransaction();
                ResultActivity.launch(this, mAccount.getPublicKey(), mTransaction);
            }
            break;
            case R.id.btn_explain: {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://vsysrate.com/wiki/vsys-coin-leasing.html"));
                mActivity.startActivity(intent);
            }
            break;
            case R.id.btn_supernode_list: {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://vsysrate.com"));
                mActivity.startActivity(intent);
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String qrContents = result.getContents();
            if (!TextUtils.isEmpty(qrContents)) {
                // scan receive address
                Operation op = null;
                if (JsonUtil.isJsonString(qrContents)) {
                    op = Operation.parse(qrContents);
                } else if (Wallet.validateAddress(qrContents)) {
                    op = new Operation(Operation.ACCOUNT);
                    op.set("address", qrContents);
                }
                if (op == null || !op.validate(Operation.ACCOUNT)) {
                    Log.e(TAG, "scan result is not an account opc");
                    UIUtil.showUnsupportQrCodeDialog(this);
                    return;
                }
                if (op.get("address") != null) {
                    String address = op.getString("address");
                    mBinding.etAddress.setText(address);
                }
                if (op.get("amount") != null) {
                    long amount = op.getLong("amount");
                    String text = null;
                    if (amount != 0) {
                        text = CoinUtil.format(amount);
                    }
                    mBinding.etAmount.setText(text);
                }
                if (op.get("invoice") != null){
                    String invoice = op.getString("invoice");
                    mBinding.etAttachment.setText(invoice);
                }
            }
        }
    }

    private void generateTransaction() {
        mTransaction = new Transaction();
        mTransaction.setTransactionType(mType);
        mTransaction.setSenderPublicKey(mAccount.getPublicKey());
        mTransaction.setAmount(CoinUtil.parse(mBinding.etAmount.getText().toString()));
        mTransaction.setRecipient(mBinding.etAddress.getText().toString());
        mTransaction.setAttachment(mBinding.etAttachment.getText().toString());
        mTransaction.setTimestamp(System.currentTimeMillis());
    }
}
