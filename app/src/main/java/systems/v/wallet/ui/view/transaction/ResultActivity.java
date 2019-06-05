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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.Base58;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.JsonUtil;
import systems.v.wallet.basic.wallet.ContractFunc;
import systems.v.wallet.basic.wallet.Operation;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.data.BaseErrorConsumer;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.api.NodeAPI;
import systems.v.wallet.data.bean.RegisterBean;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.databinding.ActivityResultBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.SPUtils;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.utils.UIUtil;
import systems.v.wallet.utils.bus.AppBus;
import systems.v.wallet.utils.bus.AppEvent;
import systems.v.wallet.utils.bus.AppEventType;
import vsys.Vsys;

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
        int type = mTransaction.getTransactionType();
        switch (type) {
            case Transaction.PAYMENT:
                mBinding.tvInfo.setText(getString(R.string.send_payment_success, CoinUtil.formatWithUnit(mTransaction.getAmount())));
                mBinding.tvAddress.setText(mTransaction.getRecipient());
                break;
            case Transaction.LEASE:
                mBinding.tvInfo.setText(getString(R.string.send_lease_success, CoinUtil.formatWithUnit(mTransaction.getAmount())));
                mBinding.tvAddress.setText(mTransaction.getRecipient());
                break;
            case Transaction.CANCEL_LEASE:
                mBinding.tvInfo.setText(getString(R.string.send_cancel_lease_success, CoinUtil.formatWithUnit(mTransaction.getAmount())));
                mBinding.tvAddress.setText(mTransaction.getRecipient());
                break;
            case Transaction.CONTRACT_REGISTER:
                mBinding.tvInfo.setText(R.string.create_token_success);
                mBinding.tvAddress.setText(Vsys.contractId2TokenId(mTransaction.getContractId(), 0));
                mBinding.tvTip.setText(R.string.create_token_success_tip);
                AppBus.getInstance().post(new AppEvent(AppEventType.TOKEN_ADD));
                break;
            case Transaction.CONTRACT_EXECUTE:
                switch (mTransaction.getActionCode()){
                    case Vsys.ActionIssue:
                        mBinding.tvInfo.setText("");
                        mBinding.tvAddress.setText(CoinUtil.format(mTransaction.getContractObj().getAmount(), mTransaction.getContractObj().getUnity()));
                        mBinding.tvTip.setText(R.string.issue_token_success);
                        mBinding.tvTip.setTextColor(getResources().getColor(R.color.text_strong));
                        break;
                    case Vsys.ActionSend:
                        mBinding.tvInfo.setText(getString(R.string.send_payment_success, CoinUtil.format(mTransaction.getContractObj().getAmount(), mTransaction.getContractObj().getUnity())));
                        mBinding.tvAddress.setText(mTransaction.getContractObj().getRecipient());
                        mBinding.tvTip.setText("");
                        break;
                    case Vsys.ActionDestroy:
                        mBinding.ivIconResult.setImageResource(R.drawable.ico_burn_big);
                        mBinding.tvInfo.setText("");
                        mBinding.tvAddress.setText(CoinUtil.format(mTransaction.getContractObj().getAmount(), mTransaction.getContractObj().getUnity()));
                        mBinding.tvTip.setText(getString(R.string.destroy_token_amount));
                        mBinding.tvTip.setTextColor(getResources().getColor(R.color.text_strong));
                        break;
                }

                break;
        }
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
        final NodeAPI api = RetrofitHelper.getInstance().getNodeAPI();
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
            case Transaction.CONTRACT_REGISTER:
                Map<String, Object> m = mTransaction.toRequestBody();
                Log.d("register tx call api", JSON.toJSONString(mTransaction));
                observable = api.registerContract(m)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .concatMap(new Function<RespBean, Observable<RespBean>>() {
                            @Override
                            public Observable<RespBean> apply(RespBean resp){
                                Log.d("register resp", JSON.toJSONString(resp));
                                if(resp.getCode() == 0){
                                    RegisterBean registerTx = JSON.parseObject(resp.getData(), RegisterBean.class);
                                    RespBean addRes = addWatchedToken(registerTx);
                                    return Observable.just(addRes);
                                }
                                return Observable.just(resp);
                            }
                        });
                break;
            case Transaction.CONTRACT_EXECUTE:
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
                        }else{
                            ToastUtil.showToast(resp.getMsg());
                        }
                    }
                }, BaseErrorConsumer.create(new BaseErrorConsumer.Callback() {
                    @Override
                    public void onError(int code, String msg) {
                        ToastUtil.showToast(String.format("Failed, please retry!(error:%s)", msg));
                    }
                }));
    }

    public RespBean addWatchedToken(RegisterBean registerTx) {
        RespBean resp = new RespBean();
        final String tokenId = Vsys.contractId2TokenId(registerTx.getContractId(), 0);
        final String texturalDescriptors = registerTx.getContract().getTextual().getDescriptors();
        if (tokenId == null || tokenId.isEmpty()) {
            resp.setCode(-1);
            resp.setMsg("error when add Token");
            return resp;
        }
        final String key = Constants.WATCHED_TOKEN.concat(mAccount.getPublicKey());
        List<Token> tokens = JSON.parseArray(SPUtils.getString(key), Token.class);
        if (tokens == null) {
            tokens = new ArrayList<>();
        }
        for (Token t : tokens) {
            if (t.equals(t.getTokenId())) {
                resp.setCode(-1);
                resp.setMsg("token existed");
            }
        }
        Token newToken = new Token();
        newToken.setTokenId(tokenId);
        newToken.setContractId(registerTx.getContractId());
        newToken.setUnity(mTransaction.getContractObj().getUnity());
        newToken.setMax(mTransaction.getContractObj().getMax());
        newToken.setDescription(mTransaction.getContractObj().getTokenDescription());
        newToken.setIssuer(mAccount.getAddress());
        List<ContractFunc> funcs = new ArrayList<>(JSON.parseArray(Vsys.decodeContractTextrue(texturalDescriptors), ContractFunc.class));
        newToken.setFuncs(funcs.toArray(new ContractFunc[funcs.size()]));
        tokens.add(newToken);
        SPUtils.setString(key, JSON.toJSONString(tokens));
        resp.setCode(0);
        resp.setData("{}");
        mTransaction.setContractId(registerTx.getContractId());
        return resp;
    }
}
