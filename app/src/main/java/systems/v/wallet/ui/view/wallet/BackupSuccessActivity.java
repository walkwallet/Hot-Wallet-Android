package systems.v.wallet.ui.view.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.databinding.ActivityBackupSuccessBinding;
import systems.v.wallet.ui.BaseActivity;
import systems.v.wallet.ui.view.main.MainActivity;


public class BackupSuccessActivity extends BaseActivity {
    public static void launch(Activity from) {
        Intent intent = new Intent(from, BackupSuccessActivity.class);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private ActivityBackupSuccessBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_backup_success);
        mBinding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.launch(mActivity, true);
                finish();
            }
        });
    }
}
