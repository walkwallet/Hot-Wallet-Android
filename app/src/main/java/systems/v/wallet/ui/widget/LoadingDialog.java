package systems.v.wallet.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import systems.v.wallet.R;

public class LoadingDialog extends Dialog {
    private Context mContext;
    private Animation animation;
    private ImageView mIv;

    public LoadingDialog(Context context) {
        super(context, R.style.LoadingDialog);
        this.mContext = context;
        init();
    }

    private void init() {
        Window window = getWindow();
        if (window != null) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_loading, null);
        mIv = view.findViewById(R.id.iv_loading);
        setContentView(view);
        animation = AnimationUtils.loadAnimation(mContext, R.anim.loading);
        LinearInterpolator lin = new LinearInterpolator();
        animation.setInterpolator(lin);
        mIv.setAnimation(animation);
        setCanceledOnTouchOutside(false);
    }

    public void show() {
        super.show();
        mIv.startAnimation(animation);
    }

    public void dismiss() {
        super.dismiss();
        mIv.clearAnimation();
    }

    public void showLoading() {
        if (!isShowing()) {
            show();
        }
    }

    public void dismissLoading() {
        if (isShowing()) {
            dismiss();
        }
    }
}
