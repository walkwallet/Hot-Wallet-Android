package systems.v.wallet.ui.view;

import android.os.Bundle;

import systems.v.wallet.R;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.ui.BaseActivity;
import systems.v.wallet.ui.view.main.MainActivity;
import systems.v.wallet.ui.view.wallet.WalletInitActivity;
import systems.v.wallet.utils.PermissionUtil;
import systems.v.wallet.utils.ToastUtil;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (!PermissionUtil.permissionGranted(this)) {
            PermissionUtil.checkPermissions(this);
        } else {
            launch();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionUtil.PERMISSION_REQUEST_CODE:
                if (!PermissionUtil.permissionGranted(this)) {
                    ToastUtil.showToast(R.string.grant_permissions);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                } else {
                    launch();
                }
        }
    }

    private void launch() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (FileUtil.walletExists(mActivity)) {
                    MainActivity.launch(SplashActivity.this, false);
                } else {
                    WalletInitActivity.launch(mActivity);
                }
                finish();
            }
        }, 500);

    }
}
