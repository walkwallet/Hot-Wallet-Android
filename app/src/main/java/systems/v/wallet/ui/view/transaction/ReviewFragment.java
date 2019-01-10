package systems.v.wallet.ui.view.transaction;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.databinding.FragmentSendReviewBinding;
import systems.v.wallet.utils.UIUtil;

public class ReviewFragment extends TransactionDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentSendReviewBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_send_review, container, false);
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNextListener != null) {
                    mNextListener.onNext();
                }
            }
        });
        binding.ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        int icon = getArguments().getInt("icon", 0);
        String title = getArguments().getString("title");
        String buttonText = getArguments().getString("buttonText");
        if (icon != 0) {
            binding.ivClose.setImageResource(icon);
        }
        if (title != null) {
            binding.tvTitle.setText(title);
        }
        if (buttonText != null) {
            binding.btnNext.setText(buttonText);
        }
        String data = getArguments().getString("tx");
        if (!TextUtils.isEmpty(data)) {
            Transaction tx = JSON.parseObject(data, Transaction.class);
            UIUtil.addTransactionDetail(inflater, binding.llInfo, tx);
        }
        return binding.getRoot();
    }

    @Override
    protected Dialog createDialog(int animResId) {
        return super.createDialog(R.style.Animation_Design_BottomSheetDialog);
    }

    public static class Builder {
        private AppCompatActivity mActivity;
        private String mTag = "review";
        private int mIconId;
        private CharSequence mTitle;
        private CharSequence mButtonText;
        private OnNextListener mNextListener;
        private DialogInterface.OnDismissListener mDismissListener;

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

        public Builder setButtonText(@StringRes int textId) {
            mButtonText = mActivity.getText(textId);
            return this;
        }

        public Builder setButtonText(@Nullable CharSequence text) {
            mButtonText = text;
            return this;
        }

        public Builder setIcon(@DrawableRes int iconId) {
            mIconId = iconId;
            return this;
        }

        public Builder setTag(String tag) {
            mTag = tag;
            return this;
        }

        public Builder setOnNextListener(OnNextListener l) {
            this.mNextListener = l;
            return this;
        }

        public Builder setOnDismissListener(DialogInterface.OnDismissListener l) {
            this.mDismissListener = l;
            return this;
        }

        public ReviewFragment create(String publicKey, Transaction tx) {
            ReviewFragment fragment = new ReviewFragment();
            Bundle bundle = new Bundle();
            bundle.putString("tx", JSON.toJSONString(tx));
            bundle.putString("publicKey", publicKey);
            if (mIconId != 0) {
                bundle.putInt("icon", mIconId);
            }
            if (mTitle != null) {
                bundle.putString("title", mTitle.toString());
            }
            if (mButtonText != null) {
                bundle.putString("buttonText", mButtonText.toString());
            }
            if (mNextListener != null) {
                fragment.setOnNextListener(mNextListener);
            }
            if (mDismissListener != null) {
                fragment.setOnDismissListener(mDismissListener);
            }
            fragment.setArguments(bundle);
            return fragment;
        }

        public ReviewFragment show(String publicKey, Transaction tx) {
            ReviewFragment fragment = create(publicKey, tx);
            fragment.show(mActivity.getSupportFragmentManager(), mTag);
            return fragment;
        }
    }
}
