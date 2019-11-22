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
import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.databinding.FragmentSendQrCodeBinding;
import systems.v.wallet.basic.utils.QRCodeUtil;

public class QrCodeFragment extends TransactionDialogFragment {

    public static QrCodeFragment newInstance(String publicKey, String data) {
        QrCodeFragment fragment = new QrCodeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("data", data);
        bundle.putString("publicKey", publicKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    private List<String> pageMessages;
    private int current;
    private FragmentSendQrCodeBinding binding;
    private final static String TAG = "QrCodeFragment";
    private String mTip = null;
    private String mNextStr = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_send_qr_code, container, false);
        String data = getArguments().getString("data");

        if(data != null && data.length() > QRCodeUtil.PageSize){
            Log.d( "page code", data);
            pageMessages = QRCodeUtil.formatPageMessages(data);
            current = 1;
            binding.tvPage.setVisibility(View.VISIBLE);
            binding.btnBack.setVisibility(View.VISIBLE);
        }else{
            Log.d("single code", data);
            binding.ivQrCode.setImageBitmap(QRCodeUtil.generateQRCode(data, 800));
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
        if(mTip == null) {
            binding.tvTip.setText(Html.fromHtml(getString(R.string.send_scan_qr_code_tip)));
        }else if(mTip.isEmpty()){
            binding.tvTip.setVisibility(View.GONE);
        }else {
            binding.tvTip.setText(mTip);
        }
        if(mNextStr != null){
            binding.btnNext.setText(mNextStr);
        }
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

    public void setTip(String tips){
        mTip = tips;
    }

    public void setBtnNextText(String text){
        mNextStr = text;
    }
}
