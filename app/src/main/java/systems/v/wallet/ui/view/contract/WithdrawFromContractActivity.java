package systems.v.wallet.ui.view.contract;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import com.alibaba.fastjson.JSON;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.Base58;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.wallet.ContractFunc;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.data.BaseErrorConsumer;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.api.NodeAPI;
import systems.v.wallet.data.bean.ContractBalanceBean;
import systems.v.wallet.data.bean.ContractBean;
import systems.v.wallet.data.bean.ContractContentBean;
import systems.v.wallet.data.bean.ContractInfoBean;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.bean.TokenBalanceBean;
import systems.v.wallet.databinding.ActivityWithdrawFromContractBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.transaction.ResultActivity;
import systems.v.wallet.ui.view.transaction.ScannerActivity;
import systems.v.wallet.utils.ClipUtil;
import systems.v.wallet.utils.ContractUtil;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.utils.UIUtil;
import vsys.Contract;
import vsys.Vsys;

public class WithdrawFromContractActivity extends BaseThemedActivity implements View.OnClickListener {

    public static void launch(Activity from, String publicKey) {
        Intent intent = new Intent(from, WithdrawFromContractActivity.class);
        intent.putExtra("publicKey", publicKey);
        intent.putExtra("type", Transaction.CONTRACT_EXECUTE);
        from.startActivity(intent);
    }

    private static final String TAG = "DepositToContractActivity";

    private ActivityWithdrawFromContractBinding mBinding;
    private Transaction mTransaction;
    private Token mToken;
    private int mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_withdraw_from_contract);

        mType = getIntent().getIntExtra("type", Transaction.CONTRACT_EXECUTE);

        initView();
        initListener();
    }

    private void initView(){
        setAppBar(mBinding.toolbar);
        mBinding.setClick(this);

        mBinding.toolbar.setTitle(R.string.withdraw_from_contract);
        mBinding.etAddress.setHint(R.string.withdraw_from_contract_hint);
        mBinding.tvSendToLabel.setText(R.string.withdraw_review_withdraw_from);
        mBinding.tvFee.setText(CoinUtil.formatWithUnit(Transaction.DEFAULT_TOKEN_TX_FEE));
        mBinding.llTransactionFee.setVisibility(View.GONE);
        mBinding.llAmount.setVisibility(View.GONE);
        mBinding.btnConfirm.setVisibility(View.GONE);
        mBinding.btnNextStep.setVisibility(View.VISIBLE);
        mBinding.tvAddressError.setVisibility(View.GONE);
    }

    private void initListener() {
        mBinding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                long value = CoinUtil.parse(mBinding.etAmount.getText().toString(), mToken.getUnity());
                if(TextUtils.isEmpty(s)){
                    mBinding.tvBalanceError.setVisibility(View.GONE);
                } else if((mToken.getBalance() < value )){
                    mBinding.tvBalanceError.setText(getString(R.string.send_insufficient_balance_error, mToken.getName() != null ? mToken.getName() : ""));
                    mBinding.tvBalanceError.setVisibility(View.VISIBLE);
                }else if(mAccount.getAvailable() < Transaction.DEFAULT_TOKEN_TX_FEE) {
                    mBinding.tvBalanceError.setText(getString(R.string.send_insufficient_balance_error, "VSYS"));
                    mBinding.tvBalanceError.setVisibility(View.VISIBLE);
                }else{
                    mBinding.tvBalanceError.setVisibility(View.GONE);
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
            case R.id.btn_next_step: {
                getAvailableBalance(mBinding.etAddress.getText().toString());
            }
            case R.id.btn_max: {
                mBinding.etAmount.setText(CoinUtil.format(mToken.getBalance(), mToken.getUnity()));
            }
            break;
            case R.id.btn_paste: {
                mBinding.etAddress.setText(ClipUtil.getClip(this));
            }
            break;
            case R.id.btn_scan:
                ScannerActivity.launch(this);
                break;
            case R.id.btn_confirm: {
                String amount = mBinding.etAmount.getText().toString();
                String address = mBinding.etAddress.getText().toString();
                String str = null;

                if (TextUtils.isEmpty(amount)) {
                    str = getString(R.string.send_amount_empty_error);
                } else if(!CoinUtil.validate(amount, mToken.getUnity())){
                    str = getString(R.string.invalid_precision);
                } else if(mAccount.getAvailable() < Transaction.DEFAULT_TOKEN_TX_FEE) {
                    str = getString(R.string.send_insufficient_balance_error, "VSYS");
                } else if(new BigDecimal(amount).multiply(BigDecimal.valueOf(mToken.getUnity())).
                        compareTo(BigDecimal.valueOf(mToken.getBalance())) > 0){
                    str = getString(R.string.send_insufficient_balance_error, mToken.getName() != null ? mToken.getName() : "");
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
                mBinding.etAddress.setText(qrContents);
                getAvailableBalance(mBinding.etAddress.getText().toString());
            }
        }
    }

    private void getAvailableBalance(final String contractId) {
        final NodeAPI nodeApi = RetrofitHelper.getInstance().getNodeAPI();
        mToken = new Token();
        Disposable d = nodeApi.contractInfo(contractId)// request contract info
                .compose(this.<RespBean>bindLoading())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .concatMap(new Function<RespBean, Observable<RespBean>>() {// request contract info
                    @Override
                    public Observable<RespBean> apply(final RespBean respBean) throws Exception {

                        if(respBean != null) {
                            ContractBean contractBean = JSON.parseObject(respBean.getData(), ContractBean.class);
                            // 非合法contract
                            if (!contractBean.getType().equals("LockContract") && !contractBean.getType().equals("PaymentChannelContract")) {
                                return Observable.create(new ObservableOnSubscribe<RespBean>() {
                                    @Override
                                    public void subscribe(ObservableEmitter<RespBean> emitter) throws Exception {
                                        emitter.onError(new Throwable(getString(R.string.invalid_lock_or_payment_channel_contract)));
                                    }
                                });
                            }
                            for (ContractInfoBean info: contractBean.getInfo()){
                                if (info.getName().equals("tokenId")){
                                    mToken.setTokenId(info.getData());
                                }else if(info.getName().equals("maker")){
                                    mToken.setMaker(info.getData());
                                }
                            }
                            return nodeApi.contractContent(Vsys.tokenId2ContractId(mToken.getTokenId()));
                        }else{
                            return Observable.create(new ObservableOnSubscribe<RespBean>() {
                                @Override
                                public void subscribe(ObservableEmitter<RespBean> emitter) throws Exception {
                                    emitter.onError(new Throwable(respBean.getMsg()));
                                }
                            });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .concatMap(new Function<RespBean, Observable<RespBean>>() {// request contract info
                    @Override
                    public Observable<RespBean> apply(final RespBean respBean) throws Exception {
                        if(respBean != null) {
                            ContractContentBean contractContent = JSON.parseObject(respBean.getData(), ContractContentBean.class);
                            List<ContractFunc> funcs = new ArrayList<>((JSON.parseArray(Vsys.decodeContractTexture(contractContent.getTextual().getDescriptors()), ContractFunc.class)));
                            mToken.setFuncs(funcs.toArray(new ContractFunc[funcs.size()]));
                            if (ContractUtil.isVsysToken(mToken.getTokenId())) {
                                return nodeApi.balance(mAccount.getAddress());
                            }else {
                                return nodeApi.tokenBalance(mAccount.getAddress(), mToken.getTokenId());
                            }
                        }else{
                            return Observable.create(new ObservableOnSubscribe<RespBean>() {
                                @Override
                                public void subscribe(ObservableEmitter<RespBean> emitter) throws Exception {
                                    emitter.onError(new Throwable(respBean.getMsg()));
                                }
                            });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .compose(this.<RespBean>bindLoading())
                .concatMap(new Function<RespBean, Observable<RespBean>>() {// request contract info
                    @Override
                    public Observable<RespBean> apply(final RespBean respBean) throws Exception {
                        if(respBean != null) {
                            if (ContractUtil.isVsysToken(mToken.getTokenId())) {
                                mToken.setUnity(Vsys.VSYS);
                            }else{
                                TokenBalanceBean tokenBalance = JSON.parseObject(respBean.getData(), TokenBalanceBean.class);
                                mToken.setUnity(tokenBalance.getUnity());
                            }
                            String dbKey = Vsys.getContractBalanceDbKey(mAccount.getAddress());
                            return nodeApi.contractData(contractId, dbKey);
                        }else{
                            return Observable.create(new ObservableOnSubscribe<RespBean>() {
                                @Override
                                public void subscribe(ObservableEmitter<RespBean> emitter) throws Exception {
                                    emitter.onError(new Throwable(respBean.getMsg()));
                                }
                            });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .concatMap(new Function<RespBean, Observable<String>>() {// request contract info
                    @Override
                    public Observable<String> apply(final RespBean respBean) throws Exception {
                        if (respBean != null && respBean.getCode() == 0) {
                            return Observable.create(new ObservableOnSubscribe<String>() {
                                @Override
                                public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                                    if (respBean.getCode() == 0) {
                                        ContractBalanceBean balance = JSON.parseObject(respBean.getData(), ContractBalanceBean.class);
                                        mToken.setBalance(balance.getValue());
                                        emitter.onNext("");
                                    } else {
                                        emitter.onNext(respBean.getMsg());
                                    }
                                }
                            });
                        }else {
                            return Observable.create(new ObservableOnSubscribe<String>() {
                                @Override
                                public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                                    emitter.onError(new Throwable(respBean.getMsg()));
                                }
                            });
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String result) throws Exception {
                        if (result.isEmpty()){
                            mBinding.tvAvailableBalance.setText(getString(R.string.send_available_balance, CoinUtil.format(mToken.getBalance(), mToken.getUnity())));
                            UIUtil.setAmountInputFilterWithScale(mBinding.etAmount, mToken.getUnity());
                            mBinding.llTransactionFee.setVisibility(View.VISIBLE);
                            mBinding.llAmount.setVisibility(View.VISIBLE);
                            mBinding.btnConfirm.setVisibility(View.VISIBLE);
                            mBinding.btnNextStep.setVisibility(View.GONE);
                            mBinding.tvAddressError.setVisibility(View.GONE);
                        } else{
                            ToastUtil.showToast("Accept result msg" + result);
                        }
                    }
                }, BaseErrorConsumer.create(new BaseErrorConsumer.Callback() {
                    @Override
                    public void onError(int code, String msg) {
                        ToastUtil.showToast(msg);
                        mBinding.tvAddressError.setVisibility(View.VISIBLE);
                        mBinding.tvAddressError.setText(getString(R.string.invalid_lock_or_payment_channel_contract));
                    }
                }));;
    }

    // token 转到合约的交易
    private void generateTransaction() {
        Contract c = new Contract();
        c.setContractId(mBinding.etAddress.getText().toString());
        c.setRecipient(mAccount.getAddress());
        c.setAmount(CoinUtil.parse(mBinding.etAmount.getText().toString(), mToken.getUnity()));
//        c.setSenderAddr(mAccount.getAddress());
        c.setUnity(mToken.getUnity());

        mTransaction = new Transaction();
        mTransaction.setActionCode(Vsys.ActionWithdraw);
        mTransaction.setContractObj(c);
        mTransaction.setContractId(Vsys.tokenId2ContractId(mToken.getTokenId()));
        mTransaction.setFee(Transaction.DEFAULT_TOKEN_TX_FEE);
        mTransaction.setTransactionType(mType);
        mTransaction.setAddress(mAccount.getAddress());
        mTransaction.setFunction(Base58.encode(c.buildWithdrawData()));
        mTransaction.setFunctionId(ContractUtil.getFuncIdxByFuncName(mToken.getFuncs(), Vsys.ActionWithdraw));
        mTransaction.setFunctionTextual(ContractUtil.getFunctionTextual(Vsys.ActionWithdraw, c.getRecipient(), c.getAmount()));
        mTransaction.setFunctionExplain(ContractUtil.getFunctionExplain(Vsys.ActionWithdraw, mBinding.etAmount.getText().toString(), c.getRecipient()));
        mTransaction.setSenderPublicKey(mAccount.getPublicKey());
        mTransaction.setTimestamp(System.currentTimeMillis());
//        mTransaction.setAttachment(mBinding.etAttachment.getText().toString());
    }
}
