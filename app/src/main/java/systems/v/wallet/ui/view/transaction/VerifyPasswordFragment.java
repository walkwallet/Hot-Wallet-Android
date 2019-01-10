package systems.v.wallet.ui.view.transaction;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.KeyboardUtil;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.databinding.FragmentVerifyPasswordBinding;
import systems.v.wallet.utils.ToastUtil;

public class VerifyPasswordFragment extends TransactionDialogFragment {

    public static VerifyPasswordFragment show(AppCompatActivity activity, String publicKey, TransactionDialogFragment.OnNextListener l) {
        VerifyPasswordFragment fragment = new VerifyPasswordFragment();
        fragment.show(activity.getSupportFragmentManager(), "verify");
        fragment.setOnNextListener(l);
        Bundle bundle = new Bundle();
        bundle.putString("publicKey", publicKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    public interface OnNextListener extends TransactionDialogFragment.OnNextListener {
        void onInput(String password);
    }

    private FragmentVerifyPasswordBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_verify_password, container, false);
        mBinding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verify();
            }
        });
        mBinding.ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        if (getArguments() != null) {
            String title = getArguments().getString("title");
            String buttonText = getArguments().getString("buttonText");
            if (!TextUtils.isEmpty(title)) {
                mBinding.tvTitle.setText(title);
            }
            if (!TextUtils.isEmpty(buttonText)) {
                mBinding.btnNext.setText(buttonText);
            }
        }

        mBinding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    mBinding.btnNext.setEnabled(false);
                } else {
                    mBinding.btnNext.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

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
                    KeyboardUtil.show(getActivity(), mBinding.etPassword);
                }
            }
        }, 50);
    }

    private void verify() {
        if (mNextListener != null) {
            String input = mBinding.etPassword.getText().toString();
            if (TextUtils.isEmpty(input)) {
                return;
            }
            Wallet wallet = App.getInstance().getWallet();
            if (wallet != null) {
                if (mNextListener instanceof OnNextListener) {
                    ((OnNextListener) mNextListener).onInput(input);
                }
                String password = App.getInstance().getWallet().getPassword();
                if (input.equals(password)) {
                    mNextListener.onNext();
                    return;
                }
                ToastUtil.showToast(R.string.password_wrong);
            } else {
                if (mNextListener instanceof OnNextListener) {
                    ((OnNextListener) mNextListener).onInput(input);
                }
            }
        }
    }
}
