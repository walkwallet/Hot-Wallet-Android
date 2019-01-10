package systems.v.wallet.ui.view.setting.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.databinding.ViewDataBinding;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.databinding.ItemAddressManagementBinding;
import systems.v.wallet.entity.AccountEntity;
import systems.v.wallet.ui.widget.wrapper.BaseAdapter;

public class AddressManagementAdapter extends BaseAdapter<AccountEntity> {

    public AddressManagementAdapter(List<AccountEntity> data, Context context) {
        super(data, context);
    }

    @Override
    public int setLayId(ViewGroup parent, int viewType) {
        return R.layout.item_address_management;
    }

    @Override
    public void setViews(ViewDataBinding viewDataBinding, int position) {
        ItemAddressManagementBinding binding = (ItemAddressManagementBinding) viewDataBinding;
        AccountEntity account = mData.get(position);
        if (account.isHeaderWallet() || account.isHeaderMonitor()) {
            binding.llHeader.setVisibility(View.VISIBLE);
            if (account.isHeaderMonitor()) {
                binding.tvHeader.setText(R.string.setting_monitor_address);
            } else {
                binding.tvHeader.setText(R.string.setting_wallet_address);
            }
        } else {
            binding.llHeader.setVisibility(View.GONE);
        }
        binding.tvAddress.setText(account.getAddress());
        binding.tvBalance.setText(CoinUtil.formatWithUnit(account.getRegular()));
    }
}
