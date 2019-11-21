package systems.v.wallet.ui.view.contract;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;

import org.w3c.dom.Text;

import java.math.BigDecimal;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.Base58;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.basic.wallet.Transaction;
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
        intent.putExtra("type", Transaction.CONTRACT_EXECUTE);
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

        initView();
    }

    private void initView(){
        String balance = CoinUtil.format(mToken.getBalance(), mToken.getUnity());
        String fee = CoinUtil.formatWithUnit(Transaction.DEFAULT_TOKEN_TX_FEE);
        setAppBar(mBinding.toolbar);
        mBinding.setClick(this);

        UIUtil.setAmountInputFilterWithScale(mBinding.etAmount, mToken.getUnity());
        mBinding.tvFee.setText(fee);
        mBinding.toolbar.setTitle(R.string.send_token_title);
        mBinding.tvAvailableBalance.setText(getString(R.string.send_available_balance, balance));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm: {
                String amount = mBinding.etAmount.getText().toString();
                String str = null;

                if (TextUtils.isEmpty(amount)) {
                    str = getString(R.string.send_amount_empty_error);
                } else if(!CoinUtil.validate(amount, mToken.getUnity())){
                    str = getString(R.string.invalid_precision);
                } else if (mAccount.getAvailable() < Transaction.DEFAULT_TOKEN_TX_FEE) {
                    str = getString(R.string.send_insufficient_balance_error, "VSYS");
                } else if( new BigDecimal(amount).multiply(BigDecimal.valueOf(mToken.getUnity())).
                        compareTo(BigDecimal.valueOf(mToken.getBalance())) > 0){
                    str = getString(R.string.send_insufficient_balance_error, mToken.getName() != null ? mToken.getName(): "");
                }
                if (str != null) {
                    new AlertDialog.Builder(mActivity)
                            .setMessage(str)
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
