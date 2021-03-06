package systems.v.wallet.ui.view.detail.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.ViewDataBinding;

import java.util.List;

import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.databinding.ItemRecordsBinding;
import systems.v.wallet.entity.RecordEntity;
import systems.v.wallet.ui.widget.wrapper.BaseAdapter;
import systems.v.wallet.utils.DateUtils;
import systems.v.wallet.utils.UIUtil;

public class RecordAdapter extends BaseAdapter<RecordEntity> {
    public RecordAdapter(List<RecordEntity> data, Context context) {
        super(data, context);
    }

    private boolean mHasHeader = false;

    @Override
    public int setLayId(ViewGroup parent, int viewType) {
        return R.layout.item_records;
    }

    @Override
    public void setViews(ViewDataBinding viewDataBinding, int position) {
        ItemRecordsBinding binding = (ItemRecordsBinding) viewDataBinding;
        RecordEntity item = mData.get(position);
        int drawableId = 0;
        int textId = R.string.records_title;
        String amount = item.getAmount() + "";
        String address = UIUtil.getMutatedAddress(item.getRecipient());
        switch (item.getRecordType()) {
            case RecordEntity.TYPE_RECEIVED:
            case RecordEntity.TYPE_MINTING:
                drawableId = R.drawable.ico_record_received;
                textId = R.string.detail_received;
                amount = "+" + CoinUtil.format(item.getAmount());
                break;
            case RecordEntity.TYPE_SENT:
                drawableId = R.drawable.ico_record_sent;
                textId = R.string.detail_sent;
                amount = "-" + CoinUtil.format(item.getAmount());
                break;
            case RecordEntity.TYPE_START_IN_LEASING:
                drawableId = R.drawable.ico_record_incoming;
                textId = R.string.detail_incoming_leasing;
                amount = CoinUtil.format(item.getAmount());
                break;
            case RecordEntity.TYPE_CANCELED_IN_LEASING:
                drawableId = R.drawable.ico_record_incoming_cancel;
                textId = R.string.detail_canceled_int_leasing;
                amount = CoinUtil.format(item.getAmount());
                break;
            case RecordEntity.TYPE_START_OUT_LEASING:
                drawableId = R.drawable.ico_record_leasing_start;
                textId = R.string.detail_start_leasing;
                amount = CoinUtil.format(item.getAmount());
                break;
            case RecordEntity.TYPE_CANCELED_OUT_LEASING:
                drawableId = R.drawable.ico_record_leasing_cancel;
                textId = R.string.detail_canceled_out_leasing;
                amount = CoinUtil.format(item.getAmount());
                break;
            case RecordEntity.TYPE_REGISTER_CONTRACT:
                textId = R.string.detail_create_contract;
                amount = "-" + CoinUtil.format(item.getFee());
                address = UIUtil.getMutatedAddress(item.getAddress());
                if(item.getStatus().equals(RecordEntity.SUCCESS_TX)){
                    drawableId = R.drawable.ic_reg_succ;
                }else{
                    drawableId = R.drawable.ic_exec_fail;
                }
                break;
            case RecordEntity.TYPE_EXECUTE_CONTRACT:
                textId = R.string.detail_execute_contract;
                amount = "-" + CoinUtil.format(item.getFee());
                address = UIUtil.getMutatedAddress(item.getAddress());
                if(item.getStatus().equals(RecordEntity.SUCCESS_TX)){
                    drawableId = R.drawable.ic_exec_succ;
                }else{
                    drawableId = R.drawable.ic_exec_fail;
                }
                break;
            case RecordEntity.TYPE_EXECUTE_CONTRACT_SENT:
                if(item.getAmount() != 0 && item.getToken() != null) {
                    textId = R.string.detail_execute_contract_sent;
                    if (item.getToken().isNft()) {
                        amount = "-" + CoinUtil.formatWithUnit(item.getAmount(), item.getToken().getUnity(), "NFT");
                    }else{
                        amount = "-" + CoinUtil.formatWithUnit(item.getAmount(), item.getToken().getUnity(), item.getToken().getName());
                    }
                    drawableId = R.drawable.ico_record_sent;
                    address = UIUtil.getMutatedAddress(item.getAddress());
                }
                break;
            case RecordEntity.TYPE_EXECUTE_CONTRACT_RECEIVED:
                if(item.getAmount() != 0 && item.getToken() != null) {
                    textId = R.string.detail_execute_contract_received;
                    if (item.getToken().isNft()) {
                        amount = "+" + CoinUtil.formatWithUnit(item.getAmount(), item.getToken().getUnity(), "NFT");
                    }else{
                        amount = "+" + CoinUtil.formatWithUnit(item.getAmount(), item.getToken().getUnity(), item.getToken().getName());
                    }
                    drawableId = R.drawable.ico_record_received;
                    address = UIUtil.getMutatedAddress(item.getAddress());
                }
                break;
            case RecordEntity.TYPE_EXECUTE_CONTRACT_DEPOSIT:
                if(item.getAmount() != 0 && item.getToken() != null) {
                    textId = R.string.detail_execute_contract_deposit;
                    amount = "-" + CoinUtil.formatWithUnit(item.getAmount(), item.getToken().getUnity(), item.getToken().getName());
                    drawableId = R.drawable.ico_record_sent;
                    address = UIUtil.getMutatedAddress(item.getAddress());
                }
                break;
            case RecordEntity.TYPE_EXECUTE_CONTRACT_WITHDRAW:
                if(item.getAmount() != 0 && item.getToken() != null) {
                    textId = R.string.detail_execute_contract_withdraw;
                    amount = "+" + CoinUtil.formatWithUnit(item.getAmount(), item.getToken().getUnity(), item.getToken().getName());
                    drawableId = R.drawable.ico_record_received;
                    address = UIUtil.getMutatedAddress(item.getAddress());
                }
                break;
            case RecordEntity.TYPE_EXECUTE_CONTRACT_DEPOSIT_VSYS:
                if(item.getAmount() != 0 && item.getToken() != null) {
                    textId = R.string.detail_execute_contract_deposit_vsys;
                    amount = "-" + CoinUtil.formatWithUnit(item.getAmount(), item.getToken().getUnity(), "");
                    drawableId = R.drawable.ico_record_sent;
                    address = UIUtil.getMutatedAddress(item.getAddress());
                }
                break;
            case RecordEntity.TYPE_EXECUTE_CONTRACT_WITHDRAW_VSYS:
                if(item.getAmount() != 0 && item.getToken() != null) {
                    textId = R.string.detail_execute_contract_withdraw_vsys;
                    amount = "+" + CoinUtil.formatWithUnit(item.getAmount(), item.getToken().getUnity(),"");
                    drawableId = R.drawable.ico_record_received;
                    address = UIUtil.getMutatedAddress(item.getAddress());
                }
                break;
        }
        binding.ivRecordType.setImageResource(drawableId);
        binding.tvRecordType.setText(textId);
        binding.tvAmount.setText(amount);
        binding.tvAddress.setText(address);
        binding.tvTime.setText(DateUtils.getShowTime(item.getFormatTimestamp()));
        if (mHasHeader && position == 0) {
            binding.llTop.setVisibility(View.VISIBLE);
        } else {
            binding.llTop.setVisibility(View.GONE);
        }
    }

    public void setHasHeader(boolean hasHeader) {
        mHasHeader = hasHeader;
    }
}
