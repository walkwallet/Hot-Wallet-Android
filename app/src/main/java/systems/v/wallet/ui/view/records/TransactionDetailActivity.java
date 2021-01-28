package systems.v.wallet.ui.view.records;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.alibaba.fastjson.JSON;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.TxUtil;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.databinding.ActivityTransactionDetailBinding;
import systems.v.wallet.databinding.ItemInfoVerticalCopyableBinding;
import systems.v.wallet.entity.RecordEntity;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.transaction.ResultActivity;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.IntentUtil;
import systems.v.wallet.utils.TxRecordUtil;
import systems.v.wallet.utils.UIUtil;

public class TransactionDetailActivity extends BaseThemedActivity implements View.OnClickListener {

    public static void launch(Activity from, String publicKey, RecordEntity data, boolean cancelable) {
        Intent intent = new Intent(from, TransactionDetailActivity.class);
        intent.putExtra("publicKey", publicKey);
        intent.putExtra("data", JSON.toJSONString(data));
        intent.putExtra("cancelable", cancelable);
        from.startActivity(intent);
    }

    private RecordEntity mRecord;
    private boolean mCancelable;
    private ActivityTransactionDetailBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_detail);
        setAppBar(mBinding.toolbar);
        String data = getIntent().getStringExtra("data");
        mCancelable = getIntent().getBooleanExtra("cancelable", false);
        if (!TextUtils.isEmpty(data)) {
            mRecord = JSON.parseObject(data, RecordEntity.class);
        }
        initView();
    }

    private void initView() {
        LayoutInflater inflater = getLayoutInflater();
        final ViewGroup container = mBinding.llInfo;
        final String senderAddress;
        if (mRecord.getProofs() == null) {
            senderAddress = mAccount.getAddress();
        } else {
            String senderPublicKey = mRecord.getProofs().get(0).getPublicKey();
            senderAddress = Wallet.getAddress(mWallet.getNetwork(), senderPublicKey);
        }
//        UIUtil.addItemVertical(inflater, container, R.string.transaction_detail_txid, mRecord.getId());
        ItemInfoVerticalCopyableBinding item =  UIUtil.addItemVerticalCopyable(mActivity, inflater, container, R.string.transaction_detail_txid, mRecord.getId());
        if (mRecord.getId() != null && !TextUtils.isEmpty(mRecord.getId())) {
            item.tvText.setTextColor(getResources().getColor(R.color.color_blue_deep));
            item.tvText.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    String baseUrl = Wallet.TEST_NET.equals(mWallet.getNetwork())? Constants.PUBLIC_API_SERVER_TEST : Constants.PUBLIC_API_SERVER;
                    IntentUtil.OpenLinkInBrowser(mActivity, baseUrl + "/transactions/" + mRecord.getId());
                }
            });
        }
        UIUtil.addItemVerticalCopyable(mActivity,inflater, container, senderAddress.equals(mAccount.getAddress()) ? R.string.send_review_my_address : R.string.send_review_from,
                senderAddress);
        if(mRecord.getRecipient() != null && !TextUtils.isEmpty(mRecord.getRecipient())){
            UIUtil.addItemVerticalCopyable(mActivity, inflater, container,
                    mAccount.getAddress().equals(mRecord.getRecipient()) ? R.string.send_review_to_my_address : R.string.transaction_detail_to,
                    mRecord.getRecipient());
        }
        UIUtil.addItemVertical(inflater, container, R.string.transaction_detail_type,
                TxRecordUtil.getTypeText(this, mRecord.getRecordType()));
        if((mRecord.getRecordType() == RecordEntity.TYPE_EXECUTE_CONTRACT_RECEIVED ||
                mRecord.getRecordType() == RecordEntity.TYPE_EXECUTE_CONTRACT_SENT ||
                mRecord.getRecordType() == RecordEntity.TYPE_EXECUTE_CONTRACT_DEPOSIT ||
                mRecord.getRecordType() == RecordEntity.TYPE_EXECUTE_CONTRACT_WITHDRAW)
                && mRecord.getToken() != null && mRecord.getAmount() != 0){
            UIUtil.addItemVertical(inflater, container, R.string.transaction_detail_amount,
                    CoinUtil.formatWithUnit(mRecord.getAmount(), mRecord.getToken().getUnity(), mRecord.getToken().getName()));
        }else{
            UIUtil.addItemVertical(inflater, container, R.string.transaction_detail_amount,
                    CoinUtil.formatWithUnit(mRecord.getAmount()));
        }
        UIUtil.addItemVertical(inflater, container, R.string.transaction_detail_fee,
                CoinUtil.formatWithUnit(mRecord.getFee()));
        UIUtil.addItemVertical(inflater, container, R.string.transaction_detail_status,
                mRecord.getStatus());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        String time = dateFormat.format(new Timestamp(mRecord.getTimestamp()));
        UIUtil.addItemVertical(inflater, container, R.string.transaction_detail_time,
                String.format("%s (%s)", time, TimeZone.getDefault().getDisplayName()));
        if (!TextUtils.isEmpty(mRecord.getAttachment())) {
            UIUtil.addItemVertical(inflater, container, R.string.transaction_detail_description,
                    TxUtil.decodeAttachment(mRecord.getAttachment()));
        }
        if (mCancelable) {
            mBinding.btnConfirm.setVisibility(View.VISIBLE);
            mBinding.setClick(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                cancelLease();
                break;
        }
    }

    private void cancelLease() {
        Transaction tx = new Transaction();
        tx.setTransactionType(Transaction.CANCEL_LEASE);
        tx.setSenderPublicKey(mAccount.getPublicKey());
        tx.setTimestamp(System.currentTimeMillis());
        tx.setTxId(mRecord.getId());
        tx.setRecipient(mRecord.getRecipient());
        tx.setAmount(mRecord.getAmount());
        ResultActivity.launch(this, mAccount.getPublicKey(), tx);
    }
}
