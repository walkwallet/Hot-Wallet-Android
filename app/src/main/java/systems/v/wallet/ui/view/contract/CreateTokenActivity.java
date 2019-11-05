package systems.v.wallet.ui.view.contract;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;


import java.math.BigDecimal;

import androidx.databinding.DataBindingUtil;

import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.Base58;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.databinding.ActivityCreateTokenBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.transaction.ResultActivity;
import systems.v.wallet.utils.ContractUtil;
import vsys.Contract;
import vsys.Vsys;

public class CreateTokenActivity extends BaseThemedActivity implements View.OnClickListener{

    public static void launch(Activity from, String publicKey) {
        Intent intent = new Intent(from, CreateTokenActivity.class);
        intent.putExtra("publicKey", publicKey);
        from.startActivity(intent);
    }

    private Transaction mTransaction;
    private final int DEFAULT_UNITY_POWER = 8;
    private int unityPower = DEFAULT_UNITY_POWER;
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
        setUnity(unityPower);
        mBinding.setClick(this);
        mBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                unityPower = i;
                setUnity(i);
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
                if(b){
                    new AlertDialog.Builder(mActivity)
                            .setTitle(R.string.warning)
                            .setMessage(R.string.create_token_spilt_alert)
                            .setPositiveButton(R.string.i_agree, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setNegativeButton(R.string.i_dont_agree, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mBinding.checkBox.setChecked(false);
                                }
                            }).confirm();
                }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_unity_plus:
                if (unityPower < 16){
                    setUnity( ++unityPower );
                }
                break;
            case R.id.btn_unity_minus:
                if (unityPower > 0){
                    setUnity( --unityPower );
                }
                break;
            case R.id.btn_confirm:
                String amount = mBinding.etTotal.getText().toString();
                BigDecimal maxValue = BigDecimal.valueOf(2).pow(63).subtract(BigDecimal.valueOf(1));
                BigDecimal decimal  = CoinUtil.parseBigDecimal(amount, (long)Math.pow(10, unityPower));
                int textId = 0;
                if (TextUtils.isEmpty(amount)) {
                    textId = R.string.send_amount_empty_error;
                } else if (mAccount.getAvailable() < Transaction.DEFAULT_CREATE_TOKEN_FEE) {
                    textId = R.string.send_insufficient_balance_error;
                } else if (decimal == null) {
                    textId = R.string.create_token_exceed_max;
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
                break;        }
    }

    private void setUnity(int unityPower){
        mBinding.setUnity(unityPower);
        mBinding.setMinUnit(getString(R.string.create_token_unitybase, unityPower, BigDecimal.valueOf(1).movePointLeft(unityPower).toPlainString()));
    }

    private void generateTransaction() {
        Contract c = ContractUtil.generateContract(
                (long)Math.pow(10, unityPower),
                CoinUtil.parse(mBinding.etTotal.getText().toString(), (long)Math.pow(10, unityPower)),
                mBinding.etTokenDescription.getText().toString(),
                isSplit);

        mTransaction = new Transaction();
        mTransaction.setFee(Transaction.DEFAULT_CREATE_TOKEN_FEE);
        mTransaction.setAddress(mAccount.getAddress());
        mTransaction.setContractObj(c);
        mTransaction.setContract(Base58.encode(c.getContract()));
        mTransaction.setContractInit(Base58.encode(c.buildRegisterData()));
        mTransaction.setContractInitTextual(ContractUtil.getFunctionTextual(Vsys.ActionInit, c.getMax(), c.getUnity(), c.getTokenDescription()));
        mTransaction.setContractInitExplain(ContractUtil.getFunctionExplain(Vsys.ActionInit, isSplit ? ContractUtil.SplitContractText : ContractUtil.NotSplitContractText, mBinding.etTotal.getText().toString()));
        mTransaction.setTransactionType(Transaction.CONTRACT_REGISTER);
        mTransaction.setSenderPublicKey(mAccount.getPublicKey());
        mTransaction.setTimestamp(System.currentTimeMillis());
        mTransaction.setDescription(mBinding.etContractDescription.getText().toString());
    }
}
