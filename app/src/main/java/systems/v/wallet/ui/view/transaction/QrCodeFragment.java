package systems.v.wallet.ui.view.transaction;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

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

    private List<String> pageMessages;
    private int current;
    private FragmentSendQrCodeBinding binding;
    private final static String TAG = "QrCodeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_send_qr_code, container, false);
        String tx = getArguments().getString("tx");

        if(tx != null && tx.length() > QRCodeUtil.pageSize){
            Log.d(TAG, "page code");
            pageMessages = QRCodeUtil.formatPageMessages(tx);
            current = 1;
            binding.tvPage.setVisibility(View.VISIBLE);
            binding.btnBack.setVisibility(View.VISIBLE);
        }else{
            Log.d(TAG, "single code");
            binding.ivQrCode.setImageBitmap(QRCodeUtil.generateQRCode(tx, 800));
            binding.tvPage.setVisibility(View.GONE);
            binding.btnBack.setVisibility(View.GONE);
        }
        setCurrentCode();
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pageMessages != null && current < pageMessages.size()){
                    ++current;
                    setCurrentCode();
                }else if (mNextListener != null) {
                    mNextListener.onNext();
                }
            }
        });
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pageMessages != null && current > 0){
                    --current;
                    setCurrentCode();
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

    private void setCurrentCode(){
        if(pageMessages != null && current <= pageMessages.size() && current > 0){
            binding.ivQrCode.setImageBitmap(QRCodeUtil.generateQRCode(pageMessages.get(current - 1), 800));
            binding.tvPage.setText(getString(R.string.send_scan_qr_code_page, current, pageMessages.size()));
        }
    }
}
