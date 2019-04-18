package systems.v.wallet.ui.view.main.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.databinding.ItemWalletBinding;
import systems.v.wallet.ui.view.main.TabWalletFragment;
import systems.v.wallet.ui.widget.wrapper.BaseAdapter;
import systems.v.wallet.utils.DisplayUtil;
import systems.v.wallet.utils.UIUtil;

public class WalletAdapter extends BaseAdapter<Account> {
    private int mType = TabWalletFragment.TYPE_WALLET;

    public WalletAdapter(List<Account> data, Context context) {
        super(data, context);
    }

    @Override
    public int setLayId(ViewGroup parent, int viewType) {
        return R.layout.item_wallet;
    }

    @Override
    public void setViews(ViewDataBinding viewDataBinding, int position) {
        ItemWalletBinding binding = (ItemWalletBinding) viewDataBinding;
        if (position == 0) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) binding.getRoot().getLayoutParams();
            params.topMargin = DisplayUtil.dp2px(mContext, 16f);
            binding.getRoot().setLayoutParams(params);
        } else {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) binding.getRoot().getLayoutParams();
            params.topMargin = 0;
            binding.getRoot().setLayoutParams(params);
        }
        Account item = mData.get(position);
        binding.tvAddress.setText(UIUtil.getMutatedAddress(item.getAddress()));
        binding.tvBalance.setText(CoinUtil.formatWithUnit(item.getRegular()));
//        String index;
//        if (position + 1 < 10) {
//            index = "0" + String.valueOf(position + 1);
//        } else {
//            index = String.valueOf(position + 1);
//        }
        binding.tvIndex.setText(item.getAlias());
        binding.tvIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Modify Alias
            }
        });
        binding.ivQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Show ARCode
            }
        });
        if (mType == TabWalletFragment.TYPE_WALLET) {
            binding.flWalletMonitor.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_gradient_wallet));
        } else {
            binding.flWalletMonitor.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_gradient_monitor));
        }
    }

    public void setType(int type) {
        mType = type;
    }
}
