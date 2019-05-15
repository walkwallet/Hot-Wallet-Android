package systems.v.wallet.ui.view.contract;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;

import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.Base58;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.databinding.ActivityIssueBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.transaction.ResultActivity;
import systems.v.wallet.utils.ContractUtil;
import vsys.Contract;
import vsys.Vsys;

public class IssueActivity extends BaseThemedActivity implements View.OnClickListener{

    public static void launch(Activity from, String publicKey, Token token) {
        Intent intent = new Intent(from, IssueActivity.class);
        intent.putExtra("publicKey", publicKey);
        intent.putExtra("token", JSON.toJSONString(token));
        intent.putExtra("type", Transaction.ContractExecute);
        from.startActivity(intent);
    }

    private ActivityIssueBinding mBinding;
    private Transaction mTransaction;
    private Token mToken;
    private int mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_issue);
        mType = getIntent().getIntExtra("type", Transaction.PAYMENT);
        mToken = JSON.parseObject(getIntent().getStringExtra("token"), Token.class);
        if(mToken == null){
            return ;
        }

        initView();
    }

    private void initView(){
        setAppBar(mBinding.toolbar);
        mBinding.setClick(this);

        String balance = CoinUtil.formatWithUnit(mAccount.getAvailable());
        mBinding.tvAvailableBalance.setText(getString(R.string.send_available_balance, balance));
        String fee = CoinUtil.formatWithUnit(3 * Transaction.DEFAULT_CREATE_TOKEN_FEE);
        mBinding.tvFee.setText(fee);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_confirm:
                String amount = mBinding.etAmount.getText().toString();
                int textId = 0;
                if (TextUtils.isEmpty(amount)) {
                    textId = R.string.send_amount_empty_error;
                } else if ((mAccount.getAvailable() - CoinUtil.parse(amount)) < Transaction.DEFAULT_FEE) {
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
                break;
        }
    }

    private void generateTransaction(){
        Contract c = new Contract();
        c.setContractId(Vsys.tokenId2ContractId(mToken.getTokenId()));
        c.setUnity(mToken.getUnity());
        c.setAmount(CoinUtil.parse(mBinding.etAmount.getText().toString(), mToken.getUnity()));
        mTransaction = new Transaction();
        mTransaction.setActionCode(Vsys.ActionIssue);
        mTransaction.setContractObj(c);
        mTransaction.setContractId(c.getContractId());
        mTransaction.setFee(Transaction.DEFAULT_TOKEN_TX_FEE);
        mTransaction.setTransactionType(mType);
        mTransaction.setAddress(mAccount.getAddress());
        mTransaction.setFunction(Base58.encode(c.buildIssueData()));
        mTransaction.setFunctionId(ContractUtil.getFuncIdxByFuncName(mToken.getFuncs(), Vsys.ActionIssue));
        mTransaction.setFunctionTextual(ContractUtil.getFunctionTextual(Vsys.ActionIssue, c.getAmount()));
        mTransaction.setFunctionExplain(ContractUtil.getFunctionExplain(Vsys.ActionIssue, mBinding.etAmount.getText().toString()));
        mTransaction.setSenderPublicKey(mAccount.getPublicKey());
        mTransaction.setTimestamp(System.currentTimeMillis());
    }
}
