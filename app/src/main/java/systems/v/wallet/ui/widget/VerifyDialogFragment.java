package systems.v.wallet.ui.widget;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.databinding.DialogContentVerifyBinding;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.KeyboardUtil;
import systems.v.wallet.basic.wallet.Wallet;

public class VerifyDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private OnNextListener mNextListener;
    private DialogContentVerifyBinding mBinding;

    public interface OnNextListener {
        void onInput(String password);

        void onNext();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String title = getString(R.string.verify_dialog_title);
        if (getArguments() != null) {
            String s = getArguments().getString("title");
            if (!TextUtils.isEmpty(s)) {
                title = s;
            }
        }
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.dialog_content_verify, null, false);
        Dialog dialog = builder.setTitle(title)
                .setCustomView(mBinding.getRoot())
                .setPositiveButton(R.string.confirm, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mNextListener != null) {
                    String input = mBinding.etPassword.getText().toString();
                    if (TextUtils.isEmpty(input)) {
                        return;
                    }
                    Wallet wallet = App.getInstance().getWallet();
                    if (wallet != null) {
                        mNextListener.onInput(input);
                        String password = App.getInstance().getWallet().getPassword();
                        if (input.equals(password)) {
                            dismiss();
                            mNextListener.onNext();
                            return;
                        }
                        ToastUtil.showToast(R.string.password_wrong);
                    } else {
                        dismiss(); // dismiss first in callback
                        mNextListener.onInput(input);
                    }
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dismiss();
                break;
        }

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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() != null) {
            KeyboardUtil.hide(getActivity(), mBinding.etPassword);
        }
    }

    @Override
    public void onStart() {
        adjustDialog();
        super.onStart();
    }

    public void setOnNextListener(OnNextListener listener) {
        mNextListener = listener;
    }

    private void adjustDialog() {
        Window window = getDialog().getWindow();
        if (window != null && getActivity() != null) {
            window.setWindowAnimations(R.style.Animation_Design_BottomSheetDialog);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.BOTTOM;
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setAttributes(lp);
        }
    }

    public static class Builder {
        private AppCompatActivity mActivity;
        private CharSequence mTitle;
        private OnNextListener mOnNextListener;

        public Builder(@NonNull AppCompatActivity activity) {
            mActivity = activity;
        }

        public Builder setTitle(@StringRes int titleId) {
            mTitle = mActivity.getText(titleId);
            return this;
        }

        public Builder setTitle(@Nullable CharSequence title) {
            mTitle = title;
            return this;
        }

        public Builder setOnNextListener(OnNextListener l) {
            this.mOnNextListener = l;
            return this;
        }

        public VerifyDialogFragment create() {
            VerifyDialogFragment fragment = new VerifyDialogFragment();
            Bundle bundle = new Bundle();
            if (mTitle != null) {
                bundle.putString("title", mTitle.toString());
            }
            if (mOnNextListener != null) {
                fragment.setOnNextListener(mOnNextListener);
            }
            fragment.setArguments(bundle);
            return fragment;
        }

        public VerifyDialogFragment show() {
            VerifyDialogFragment fragment = create();
            fragment.show(mActivity.getSupportFragmentManager(), "verify");
            return fragment;
        }
    }
}