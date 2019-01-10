package systems.v.wallet.ui.view.transaction;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import systems.v.wallet.R;
import systems.v.wallet.ui.BaseThemedDialogFragment;

public class TransactionDialogFragment extends BaseThemedDialogFragment {

    public interface OnNextListener {
        void onNext();
    }

    protected OnNextListener mNextListener;
    protected DialogInterface.OnDismissListener mDismissListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return createDialog(R.style.Animation_SlideRight);
    }

    @Override
    public void onStart() {
        adjustDialog();
        super.onStart();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mDismissListener != null) {
            mDismissListener.onDismiss(dialog);
        }
    }

    protected Dialog createDialog(@StyleRes int animResId) {
        Dialog dialog = new Dialog(getActivity(), getTheme());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (animResId != 0) {
            window.setWindowAnimations(animResId);
        }
        return dialog;
    }

    public void setOnNextListener(OnNextListener listener) {
        mNextListener = listener;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        mDismissListener = listener;
    }

    protected void adjustDialog() {
        Window window = getDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.BOTTOM;
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = getResources().getDimensionPixelOffset(R.dimen.transaction_dialog_height);
            window.setAttributes(lp);
        }
    }
}
