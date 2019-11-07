package systems.v.wallet.ui.view.contract.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.ViewDataBinding;


import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import systems.v.wallet.R;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.databinding.ItemAddTokenBinding;
import systems.v.wallet.ui.widget.wrapper.BaseAdapter;
import systems.v.wallet.utils.UIUtil;

public class AddTokenAdapter extends BaseAdapter<Token> {

    public AddTokenAdapter(List<Token> data, Context context) {
        super(data, context);
    }

    @Override
    public int setLayId(ViewGroup parent, int viewType) {
        return R.layout.item_add_token;
    }

    @Override
    public void setViews(ViewDataBinding viewDataBinding, int position) {
        ItemAddTokenBinding binding = (ItemAddTokenBinding) viewDataBinding;
        final Token token = mData.get(position);
        if(token.isAdded()){
            Picasso.get()
                    .load(R.drawable.ico_added_small)
                    .into(binding.ivAdded);
            binding.tvAdded.setTextColor(getContext().getResources().getColor(R.color.gray2));
            binding.tvAdded.setText(R.string.add_token_added);
            binding.llAdded.setBackground(null);
        }else{
            Picasso.get()
                    .load(R.drawable.ico_add_token_small)
                    .into(binding.ivAdded);
            binding.tvAdded.setTextColor(getContext().getResources().getColor(R.color.orange3));
            binding.tvAdded.setText(R.string.add_token_add);
            binding.llAdded.setBackground(getContext().getResources().getDrawable(R.drawable.border_gray_radius_4));
            binding.llAdded.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToken(token);
                }
            });
        }

        if (token.getName() == null || token.getName().isEmpty()){
            binding.tvTokenName.setText(UIUtil.getMutatedAddress(token.getTokenId()));
        }else{
            binding.tvTokenName.setText(token.getName());
        }

        Picasso.get()
                .load(token.getIcon())
//                .placeholder(R.drawable.ico_token)
                .transform(new CropCircleTransformation())
                .into(binding.ivTokenIcon);


    }

    public void addToken(Token token){

    }
}
