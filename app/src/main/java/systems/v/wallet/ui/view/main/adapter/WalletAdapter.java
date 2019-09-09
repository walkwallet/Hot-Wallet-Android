package systems.v.wallet.ui.view.main.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.QRCodeUtil;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.databinding.ItemWalletBinding;
import systems.v.wallet.ui.view.main.TabWalletFragment;
import systems.v.wallet.ui.widget.wrapper.BaseAdapter;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.DisplayUtil;
import systems.v.wallet.utils.SPUtils;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.utils.UIUtil;
import systems.v.wallet.utils.bus.AppBus;
import systems.v.wallet.utils.bus.AppEvent;
import systems.v.wallet.utils.bus.AppEventType;

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
    public void setViews(ViewDataBinding viewDataBinding, final int position) {
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
        final Account item = mData.get(position);
        binding.tvAddress.setText(UIUtil.getMutatedAddress(item.getAddress()));
        binding.tvBalance.setText(CoinUtil.formatWithUnit(item.getRegular()));
        if(item.getAlias() != null && !item.getAlias().isEmpty()){
            binding.tvIndex.setText(item.getAlias());
        }else {
            String index;
            if (position + 1 < 10) {
                index = "0" + String.valueOf(position + 1);
            } else {
                index = String.valueOf(position + 1);
            }

            binding.tvIndex.setText(index);
        }
        binding.tvIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Modify Alias
                final Dialog dialog = new Dialog(mContext, R.style.Dialog_Large_Orange);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_alias);
                dialog.setCancelable(true);
                Window window = dialog.getWindow();
                window.setGravity(Gravity.CENTER);
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.getDecorView().setPadding(0, 0, 0, 0);
                window.setAttributes(params);
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                TextView address = dialog.findViewById(R.id.tv_address);
                ImageView ivClose = dialog.findViewById(R.id.iv_close);
                TextView tvIndex = dialog.findViewById(R.id.tv_index);
                final EditText etAlias = dialog.findViewById(R.id.et_alias);
                etAlias.setText(item.getAlias());
                Button btnConfirm = dialog.findViewById(R.id.btn_confirm);
                tvIndex.setText(position + 1 + "");
                address.setText(mData.get(position).getAddress());
                ivClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String alias = etAlias.getText().toString();
                        if(alias.isEmpty()){
                            ToastUtil.showToast("Empty alias!");
                        }else if(item.getAlias().equals(alias)){
                            ToastUtil.showToast("Same alias!");
                        }else{
                            Map<String, String> m = new HashMap<>();;
                            String strAlias = SPUtils.getString(Constants.ALIAS);
                            if(!strAlias.isEmpty()){
                                m = JSON.parseObject(strAlias, Map.class);
                            }
                            m.put(item.getAddress(), alias);
                            SPUtils.setString(Constants.ALIAS, JSON.toJSONString(m));
                            AppBus.getInstance().post(new AppEvent(AppEventType.MODIFY_ALIAS));
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
            }
        });
        binding.ivQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Show QRCode

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
