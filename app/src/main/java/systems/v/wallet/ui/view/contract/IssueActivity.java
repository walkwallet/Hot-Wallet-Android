package systems.v.wallet.ui.view.contract;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;

import java.math.BigDecimal;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.R;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.Base58;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.data.BaseErrorConsumer;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.api.NodeAPI;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.bean.TokenBean;
import systems.v.wallet.databinding.ActivityIssueBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.transaction.ResultActivity;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.ContractUtil;
import systems.v.wallet.utils.SPUtils;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.utils.UIUtil;
import vsys.Contract;
import vsys.Vsys;

public class IssueActivity extends BaseThemedActivity implements View.OnClickListener{

    public static void launch(Activity from, String publicKey, Token token) {
        Intent intent = new Intent(from, IssueActivity.class);
        intent.putExtra("publicKey", publicKey);
        intent.putExtra("token", JSON.toJSONString(token));
        intent.putExtra("type", Transaction.CONTRACT_EXECUTE);
        from.startActivity(intent);
    }

    private ActivityIssueBinding mBinding;
    private Transaction mTransaction;
    private Token mToken;
    private int mType;
    private long mIssueMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_issue);
        mType = getIntent().getIntExtra("type", Transaction.PAYMENT);
        mToken = JSON.parseObject(getIntent().getStringExtra("token"), Token.class);
        if(mToken == null){
            return ;
        }

        initData();
        initView();
    }

    private void initView(){
        setAppBar(mBinding.toolbar);
        mBinding.setClick(this);

        UIUtil.setAmountInputFilterWithScale(mBinding.etAmount, mToken.getUnity());
        mBinding.tvAvailableBalance.setText(getString(R.string.issue_available_balance, CoinUtil.format(mIssueMax, mToken.getUnity())));
        String fee = CoinUtil.formatWithUnit(Transaction.DEFAULT_TOKEN_TX_FEE);
        mBinding.tvFee.setText(fee);
    }

    private void initData() {
        mIssueMax = mToken.getMax() - mToken.getIssuedAmount();//cached data

        final NodeAPI nodeApi = RetrofitHelper.getInstance().getNodeAPI();
        final String key = Constants.WATCHED_TOKEN.concat(mAccount.getPublicKey());
        Disposable d = nodeApi.tokenInfo(mToken.getTokenId())
                .compose(this.<RespBean>bindLoading())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RespBean>() {
                    @Override
                    public void accept(RespBean respBean) throws Exception {
                        TokenBean token = JSON.parseObject(respBean.getData(), TokenBean.class);
                        mToken.setIssuedAmount(token.getTotal());
                        List<Token> tokens = JSON.parseArray(SPUtils.getString(key), Token.class);
                        if (tokens != null) {
                            for (int i=0;i < tokens.size();i++){
                                if(tokens.get(i).getTokenId().equals(mToken.getTokenId())){
                                    tokens.set(i, mToken);
                                }
                            }

                            SPUtils.setString(key, JSON.toJSONString(tokens));
                        }
                        mIssueMax = mToken.getMax() - mToken.getIssuedAmount();
                        mBinding.tvAvailableBalance.setText(getString(R.string.issue_available_balance, CoinUtil.format(mIssueMax, mToken.getUnity())));
                    }
                }, BaseErrorConsumer.create(new BaseErrorConsumer.Callback() {
                    @Override
                    public void onError(int code, String msg) {
                        ToastUtil.showToast(msg);

                    }
                }));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_confirm:
                String amount = mBinding.etAmount.getText().toString();
                String str = null;
                if (TextUtils.isEmpty(amount)) {
                    str = getString(R.string.send_amount_empty_error);
                } else if(!CoinUtil.validate(amount, mToken.getUnity())){
                    str = getString(R.string.invalid_precision);
                } else if (mAccount.getAvailable() < Transaction.DEFAULT_TOKEN_TX_FEE) {
                    str = getString(R.string.send_insufficient_balance_error, "VSYS");
                } else if( new BigDecimal(amount).multiply(BigDecimal.valueOf(mToken.getUnity())).
                        compareTo(BigDecimal.valueOf(mIssueMax)) > 0){
                    str = getString(R.string.issue_larger_than_max);
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
                break;
            case R.id.btn_max:
                mBinding.etAmount.setText(CoinUtil.format(mIssueMax, mToken.getUnity()));
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
        mTransaction.setAttachment("");
    }
}
