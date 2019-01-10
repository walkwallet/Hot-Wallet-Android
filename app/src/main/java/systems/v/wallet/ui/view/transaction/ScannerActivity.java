package systems.v.wallet.ui.view.transaction;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureManager;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.databinding.ActivityScannerBinding;
import systems.v.wallet.ui.BaseActivity;

public class ScannerActivity extends BaseActivity {
    private static final String TAG = "ScannerActivity";

    private ActivityScannerBinding mBinding;
    private CaptureManager capture;

    public static void launch(Activity from) {
        IntentIntegrator integrator = new IntentIntegrator(from);
        integrator.setCaptureActivity(ScannerActivity.class);
        integrator.setBeepEnabled(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.initiateScan();
        openAnimVertical(from);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_scanner);
        capture = new CaptureManager(this, mBinding.barcodeScanner);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
        if (hasFlash()) {
            mBinding.ivTorch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBinding.ivTorch.getTag() != null) {
                        mBinding.ivTorch.setTag(null);
                        mBinding.barcodeScanner.setTorchOff();
                        mBinding.ivTorch.setImageResource(R.drawable.ico_torch_off);
                    } else {
                        mBinding.ivTorch.setTag(true);
                        mBinding.barcodeScanner.setTorchOn();
                        mBinding.ivTorch.setImageResource(R.drawable.ico_torch_on);
                    }
                }
            });
        } else {
            mBinding.ivTorch.setVisibility(View.GONE);
        }

        mBinding.ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outstate) {
        super.onSaveInstanceState(outstate);
        capture.onSaveInstanceState(outstate);
    }

    @Override
    public void finish() {
        super.finish();
        closeAnimVertical(mActivity);
    }

    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
}
