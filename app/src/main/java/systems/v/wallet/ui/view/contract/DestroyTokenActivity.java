package systems.v.wallet.ui.view.contract;

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
import systems.v.wallet.databinding.ActivityDestroyTokenBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.transaction.ResultActivity;
import systems.v.wallet.utils.ContractUtil;
import systems.v.wallet.utils.UIUtil;
import vsys.Contract;
import vsys.Vsys;

public class DestroyTokenActivity extends BaseThemedActivity implements View.OnClickListener {

    public static void launch(Activity from, String publicKey, Token token) {
        Intent intent = new Intent(from, DestroyTokenActivity.class);
        intent.putExtra("publicKey", publicKey);
        intent.putExtra("token", JSON.toJSONString(token));
        intent.putExtra("type", Transaction.ContractExecute);
        from.startActivity(intent);
    }

    private static final String TAG = "DestroyActivity";

    private ActivityDestroyTokenBinding mBinding;
    private Transaction mTransaction;
    private Token mToken;
    private int mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_destroy_token);

        mType = getIntent().getIntExtra("type", Transaction.PAYMENT);
        mToken = JSON.parseObject(getIntent().getStringExtra("token"), Token.class);
        // set balance and fee
        String balance = CoinUtil.format(mToken.getBalance(), mToken.getUnity());
        String fee = CoinUtil.formatWithUnit(Transaction.DEFAULT_TOKEN_TX_FEE);

        setAppBar(mBinding.toolbar);
        mBinding.setClick(this);

        mBinding.tvFee.setText(fee);
        mBinding.toolbar.setTitle(R.string.send_token_title);
        mBinding.tvAvailableBalance.setText(getString(R.string.send_available_balance, balance));

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm: {
                String amount = mBinding.etAmount.getText().toString();
                int textId = 0;

                if (TextUtils.isEmpty(amount)) {
                    textId = R.string.send_amount_empty_error;
                } else if ((mAccount.getAvailable() - CoinUtil.parse(amount)) < Transaction.DEFAULT_CREATE_TOKEN_FEE) {
                    textId = R.string.send_insufficient_balance_error;
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
                generateTransaction();

                ResultActivity.launch(this, mAccount.getPublicKey(), mTransaction);
            }
            break;
        }
    }

    private void generateTransaction() {
        Contract c = new Contract();
        c.setContractId(Vsys.tokenId2ContractId(mToken.getTokenId()));
        c.setUnity(mToken.getUnity());
        c.setAmount(CoinUtil.parse(mBinding.etAmount.getText().toString(), mToken.getUnity()));
        mTransaction = new Transaction();
        mTransaction.setActionCode(Vsys.ActionDestroy);
        mTransaction.setContractObj(c);
        mTransaction.setContractId(c.getContractId());
        mTransaction.setFee(Transaction.DEFAULT_TOKEN_TX_FEE);
        mTransaction.setTransactionType(mType);
        mTransaction.setAddress(mAccount.getAddress());
        mTransaction.setFunction(Base58.encode(c.buildDestroyData()));
        mTransaction.setFunctionId(ContractUtil.getFuncIdxByFuncName(mToken.getFuncs(), Vsys.ActionDestroy));
        mTransaction.setFunctionTextual(ContractUtil.getFunctionTextual(Vsys.ActionDestroy, c.getAmount()));
        mTransaction.setFunctionExplain(ContractUtil.getFunctionExplain(Vsys.ActionDestroy, mBinding.etAmount.getText().toString()));
        mTransaction.setSenderPublicKey(mAccount.getPublicKey());
        mTransaction.setTimestamp(System.currentTimeMillis());
    }
}
