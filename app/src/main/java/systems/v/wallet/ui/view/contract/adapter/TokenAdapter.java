package systems.v.wallet.ui.view.contract.adapter;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.databinding.ItemTokenBinding;
import systems.v.wallet.ui.widget.wrapper.BaseAdapter;
import systems.v.wallet.utils.DisplayUtil;

public class TokenAdapter extends BaseAdapter<Token> {

    public TokenAdapter(List<Token> data, Context context) {
        super(data, context);
    }

    @Override
    public int setLayId(ViewGroup parent, int viewType) {
        return R.layout.item_token;
    }

    @Override
    public void setViews(ViewDataBinding viewDataBinding, int position) {
        ItemTokenBinding binding = (ItemTokenBinding) viewDataBinding;
//            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) binding.getRoot().getLayoutParams();
//            params.topMargin = 0;
//            binding.getRoot().setLayoutParams(params);

        Token item = mData.get(position);
        if (item.getName() == null || item.getName().isEmpty()){
            binding.tvTokenName.setText(item.getTokenId());
        }else{
            binding.tvTokenName.setText(item.getName());
        }

        String balanceStr = CoinUtil.format(item.getBalance(), item.getUnity());
        Log.d("dbalance", balanceStr);
        binding.tvTokenBalance.setText( balanceStr);

        Picasso.get().load(R.drawable.ico_token)
                .transform(new CropCircleTransformation())
                .into(binding.ivTokenIcon);
    }
}
