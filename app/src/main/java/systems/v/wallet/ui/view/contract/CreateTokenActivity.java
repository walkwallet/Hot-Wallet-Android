package systems.v.wallet.ui.view.contract;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.alibaba.fastjson.JSON;

import java.util.Locale;

import androidx.databinding.DataBindingUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.R;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.Base58;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.TxUtil;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.bean.TokenBean;
import systems.v.wallet.databinding.ActivityCreateTokenBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.transaction.ResultActivity;
import systems.v.wallet.utils.UIUtil;
import vsys.Contract;
import vsys.Vsys;

public class CreateTokenActivity extends BaseThemedActivity implements View.OnClickListener{

    public static void launch(Activity from, String publicKey) {
        Intent intent = new Intent(from, CreateTokenActivity.class);
        intent.putExtra("publicKey", publicKey);
        from.startActivity(intent);
    }

    private Transaction mTransaction;
    private int unityPower = 0;
    private boolean isSplit = false;

    private final String TAG = CreateTokenActivity.class.getSimpleName();

    private ActivityCreateTokenBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_token);

        initView();
    }

    private void initView(){
        setAppBar(mBinding.toolbar);
        mBinding.setClick(this);
        mBinding.setUnity(unityPower);
        mBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mBinding.setUnity(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBinding.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isSplit = b;
            }
        });

        SpannableString spannableString = new SpannableString("100");
        SuperscriptSpan superscriptSpan = new SuperscriptSpan();
        RelativeSizeSpan sizeSpan = new RelativeSizeSpan(0.6f);
        spannableString.setSpan(superscriptSpan, 2, 3, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(sizeSpan, 2, 3, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mBinding.tvUnityMin.setText(spannableString);
        spannableString = new SpannableString("1016");
        spannableString.setSpan(superscriptSpan, 2, 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(sizeSpan, 2, 4, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mBinding.tvUnityMax.setText(spannableString);

        String balance = CoinUtil.formatWithUnit(mAccount.getAvailable());
        mBinding.tvAvailableBalance.setText(getString(R.string.send_available_balance, balance));
        String fee = CoinUtil.formatWithUnit(Transaction.DEFAULT_CREATE_TOKEN_FEE);
        mBinding.tvFee.setText(fee);
    }

    private void getTokenBalance() {
        Disposable d = RetrofitHelper.getInstance().getNodeAPI().tokenInfo( "2GaWFxW4Tc13a6gyZVYDbNTAspFgzzTHWvvqXLr3H")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RespBean>() {
                    @Override
                    public void accept(RespBean respBean) throws Exception {
                        Log.d(TAG, JSON.toJSONString(respBean));
                        TokenBean tokenBean = JSON.parseObject(respBean.getData(), TokenBean.class);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.v("derror", throwable.getMessage());
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_unity_plus:
                if (unityPower < 16){
                    ++unityPower;
                    mBinding.setUnity( unityPower );
                }
                break;
            case R.id.btn_unity_minus:
                if (unityPower > 0){
                    --unityPower;
                    mBinding.setUnity( unityPower );
                }
                break;
            case R.id.btn_confirm:
                String amount = mBinding.etTotal.getText().toString();
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

    private void generateTransaction() {
        Contract c = new Contract();
        c.setMax(CoinUtil.parse(mBinding.etTotal.getText().toString()));
        c.setUnity(10 ^ unityPower);
        c.setTokenDescription(mBinding.etDescription.getText().toString());
        c.setContract(isSplit ? Base58.decode(Vsys.ConstContractSplit): Base58.decode(Vsys.ConstContractDefault));
        mTransaction = new Transaction();
        mTransaction.setContract(c);
        mTransaction.setTransactionType(Transaction.ContractRegister);
        mTransaction.setSenderPublicKey(mAccount.getPublicKey());
        mTransaction.setTimestamp(System.currentTimeMillis());
    }
}