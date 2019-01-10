package systems.v.wallet.ui.view.transaction;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.KeyboardUtil;
import systems.v.wallet.databinding.FragmentAmountInputBinding;

public class AmountInputFragment extends TransactionDialogFragment {

    public static AmountInputFragment show(AppCompatActivity activity, String publicKey, TransactionDialogFragment.OnNextListener l) {
        AmountInputFragment fragment = new AmountInputFragment();
        fragment.show(activity.getSupportFragmentManager(), "verify");
        fragment.setOnNextListener(l);
        Bundle bundle = new Bundle();
        bundle.putString("publicKey", publicKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    public interface OnNextListener extends TransactionDialogFragment.OnNextListener {
        void onInput(String s);
    }

    private FragmentAmountInputBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_amount_input, container, false);
        mBinding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNextListener != null) {
                    String input = mBinding.etAmount.getText().toString();
                    if (TextUtils.isEmpty(input)) {
                        return;
                    }
                    dismiss();
                    if (mNextListener instanceof OnNextListener) {
                        ((OnNextListener) mNextListener).onInput(input);
                    }
                }
            }
        });

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    KeyboardUtil.show(getActivity(), mBinding.etAmount);
                }
            }
        }, 50);
    }

    @Override
    protected Dialog createDialog(@StyleRes int animResId) {
        return super.createDialog(R.style.Animation_Design_BottomSheetDialog);
    }
}
