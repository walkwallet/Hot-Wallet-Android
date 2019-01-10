package systems.v.wallet.ui.view.detail;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.QRCodeUtil;
import systems.v.wallet.databinding.ActivityReceiveBinding;
import systems.v.wallet.databinding.LayoutReceiveAddressQrCodeBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.transaction.AmountInputFragment;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.utils.UIUtil;

public class ReceiveActivity extends BaseThemedActivity {

    public static void launch(Activity from, String publicKey) {
        Intent intent = new Intent(from, ReceiveActivity.class);
        intent.putExtra("publicKey", publicKey);
        from.startActivity(intent);
    }

    private ActivityReceiveBinding mBinding;
    private String mAmount;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_receive);
        setAppBar(mBinding.toolbar);

        mBinding.tvAddress.setText(mAccount.getAddress());
        generateQrCode();
        mBinding.ivQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBitmap != null) {
                    LayoutReceiveAddressQrCodeBinding binding = DataBindingUtil.inflate(
                            getLayoutInflater(), R.layout.layout_receive_address_qr_code, null, false);
                    binding.ivQrCode.setImageBitmap(mBitmap);
                    binding.tvAddress.setText(mAccount.getAddress());
                    saveImage(binding.getRoot());
                }
            }
        });
        mBinding.tvSpecificAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmountInputFragment.show(ReceiveActivity.this, mAccount.getPublicKey(),
                        new AmountInputFragment.OnNextListener() {
                            @Override
                            public void onInput(String s) {
                                mAmount = s;
                                generateQrCode();
                            }

                            @Override
                            public void onNext() {

                            }
                        });
            }
        });
        mBinding.tvCopyAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("address", mAccount.getAddress()));
                ToastUtil.showToast(R.string.receive_copy_success);
            }
        });
    }

    private void generateQrCode() {
        long amount = CoinUtil.parse(mAmount);
        if (amount <= 0) {
            mAmount = "";
        }
        if (TextUtils.isEmpty(mAmount)) {
            mBinding.tvAmount.setText("");
            mBinding.tvAmount.setVisibility(View.GONE);
        } else {
            mBinding.tvAmount.setText(mAmount);
            mBinding.tvAmount.setVisibility(View.VISIBLE);
        }
        String msg = QRCodeUtil.getReceiveAddressStr(mAccount.getAddress(), amount);
        mBitmap = QRCodeUtil.generateQRCode(msg, 800);
        mBinding.ivQrCode.setImageBitmap(mBitmap);
    }

    private void saveImage(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        UIUtil.saveBitmap2Gallery(mActivity, bitmap, "" + System.currentTimeMillis());
    }
}
