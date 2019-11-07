package systems.v.wallet.ui.view.contract;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import java.math.BigDecimal;
import java.text.Format;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.Base58;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.JsonUtil;
import systems.v.wallet.basic.wallet.Operation;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.databinding.ActivitySendTokenBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.transaction.ResultActivity;
import systems.v.wallet.ui.view.transaction.ScannerActivity;
import systems.v.wallet.utils.ContractUtil;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.utils.UIUtil;
import vsys.Contract;
import vsys.Vsys;

public class SendTokenActivity extends BaseThemedActivity implements View.OnClickListener {

    public static void launch(Activity from, String publicKey, Token token) {
        Intent intent = new Intent(from, SendTokenActivity.class);
        intent.putExtra("publicKey", publicKey);
        intent.putExtra("token", JSON.toJSONString(token));
        intent.putExtra("type", Transaction.CONTRACT_EXECUTE);
        from.startActivity(intent);
    }

    private static final String TAG = "SendTokenActivity";

    private ActivitySendTokenBinding mBinding;
    private Transaction mTransaction;
    private Token mToken;
    private int mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_send_token);

        mType = getIntent().getIntExtra("type", Transaction.PAYMENT);
        mToken = JSON.parseObject(getIntent().getStringExtra("token"), Token.class);

        initView();
        initListener();
    }

    private void initView(){
        setAppBar(mBinding.toolbar);
        mBinding.setClick(this);

        mBinding.tvFee.setText(CoinUtil.formatWithUnit(Transaction.DEFAULT_TOKEN_TX_FEE));
        mBinding.toolbar.setTitle(R.string.send_token_title);
        mBinding.tvSendToLabel.setText(R.string.send_payment_to);
        mBinding.etAddress.setHint(R.string.send_address_input_hint);
        mBinding.tvAvailableBalance.setText(getString(R.string.send_available_balance, CoinUtil.format(mToken.getBalance(), mToken.getUnity())));
    }

    private void initListener() {
        UIUtil.setAmountInputFilterWithScale(mBinding.etAmount, mToken.getUnity());
        mBinding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                long value = CoinUtil.parse(mBinding.etAmount.getText().toString(), mToken.getUnity());
                if(TextUtils.isEmpty(s)){
                    mBinding.tvBalanceError.setVisibility(View.GONE);
                }else if((mToken.getBalance() >= value) && mAccount.getAvailable() >= Transaction.DEFAULT_TOKEN_TX_FEE) {
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
                mBinding.etAmount.setText(CoinUtil.format(mToken.getBalance(), mToken.getUnity()));
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
                } else if(!CoinUtil.validate(amount, mToken.getUnity())){
                    textId = R.string.invalid_precision;
                } else if(mAccount.getAvailable() < Transaction.DEFAULT_TOKEN_TX_FEE) {
                    textId = R.string.send_insufficient_balance_error;
                } else if(new BigDecimal(amount).multiply(BigDecimal.valueOf(mToken.getUnity())).
                        compareTo(BigDecimal.valueOf(mToken.getBalance())) > 0){
                    textId = R.string.send_insufficient_balance_error;
                } else if (!Wallet.validateAddress(address)) {
                    textId = R.string.send_address_input_error;
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
//                mTransaction.sign(mAccount);
                ResultActivity.launch(this, mAccount.getPublicKey(), mTransaction);
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
        Contract c = new Contract();
        c.setContractId(Vsys.tokenId2ContractId(mToken.getTokenId()));
        c.setUnity(mToken.getUnity());
        c.setAmount(CoinUtil.parse(mBinding.etAmount.getText().toString(), mToken.getUnity()));
        c.setRecipient(mBinding.etAddress.getText().toString());
        mTransaction = new Transaction();
        mTransaction.setActionCode(Vsys.ActionSend);
        mTransaction.setContractObj(c);
        mTransaction.setContractId(c.getContractId());
        mTransaction.setFee(Transaction.DEFAULT_TOKEN_TX_FEE);
        mTransaction.setTransactionType(mType);
        mTransaction.setAddress(mAccount.getAddress());
        mTransaction.setFunction(Base58.encode(c.buildSendData()));
        mTransaction.setFunctionId(ContractUtil.getFuncIdxByFuncName(mToken.getFuncs(), Vsys.ActionSend));
        mTransaction.setFunctionTextual(ContractUtil.getFunctionTextual(Vsys.ActionSend, c.getRecipient(), c.getAmount()));
        mTransaction.setFunctionExplain(ContractUtil.getFunctionExplain(Vsys.ActionSend, mBinding.etAmount.getText().toString(), c.getRecipient()));
        mTransaction.setSenderPublicKey(mAccount.getPublicKey());
        mTransaction.setTimestamp(System.currentTimeMillis());
        mTransaction.setAttachment(mBinding.etAttachment.getText().toString());
    }
}
