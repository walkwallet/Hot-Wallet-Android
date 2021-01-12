package systems.v.wallet.ui.view.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import systems.v.wallet.R;
import systems.v.wallet.databinding.ActivitySignMessageBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.utils.UIUtil;

public class SignMessageActivity extends BaseThemedActivity implements View.OnClickListener {

    private static final String TAG = "SignMessageActivity";
    private ActivitySignMessageBinding mBinding;
    private String mSignMsg;
    private String lastSignatureBase64Text;

    public static void launch(Activity from, String publicKey) {
        Intent intent = new Intent(from, SignMessageActivity.class);
        intent.putExtra("publicKey", publicKey);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_message);
        setAppBar(mBinding.toolbar);
        mBinding.setClick(this);
        initView();
    }

    private void initView() {
        mBinding.llSignature.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sign:
                mSignMsg = mBinding.etSignMsg.getText().toString();
                if (mSignMsg.isEmpty()) {
                    ToastUtil.showToast(R.string.sign_message_toast_empty);
                    return;
                }
                if (!validateSignMsg(mSignMsg)) {
                    ToastUtil.showLongToast(R.string.sign_message_toast_wrong_content);
                    return;
                }
                mBinding.llSignature.setVisibility(View.VISIBLE);
                lastSignatureBase64Text = mAccount.getSignature(mSignMsg.getBytes());
                mBinding.tvSignature.setText(lastSignatureBase64Text);
                break;
            case R.id.ll_signature:
                UIUtil.copyToClipboard(mActivity, lastSignatureBase64Text);
                break;
        }
    }

    private boolean validateSignMsg(String msg) {
        return msg != null && msg.matches("^[a-zA-Z0-9-/@#$%^&_+=() ]*$");
    }

}
