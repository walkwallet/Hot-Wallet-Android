package systems.v.wallet.ui.view.transaction;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.databinding.FragmentSendQrCodeBinding;
import systems.v.wallet.basic.utils.QRCodeUtil;

public class QrCodeFragment extends TransactionDialogFragment {

    public static QrCodeFragment newInstance(String publicKey, String tx) {
        QrCodeFragment fragment = new QrCodeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("tx", tx);
        bundle.putString("publicKey", publicKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentSendQrCodeBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_send_qr_code, container, false);
        String tx = getArguments().getString("tx");
        binding.ivQrCode.setImageBitmap(QRCodeUtil.generateQRCode(tx, 800));
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNextListener != null) {
                    mNextListener.onNext();
                }
            }
        });
        binding.tvTip.setText(Html.fromHtml(getString(R.string.send_scan_qr_code_tip)));
        binding.ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return binding.getRoot();
    }
}
