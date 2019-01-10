package systems.v.wallet.ui.view.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import systems.v.wallet.BuildConfig;
import systems.v.wallet.R;
import systems.v.wallet.databinding.ActivityAboutUsBinding;
import systems.v.wallet.ui.BaseActivity;

public class AboutUsActivity extends BaseActivity implements View.OnClickListener {
    public static void launch(Activity from) {
        Intent intent = new Intent(from, AboutUsActivity.class);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private ActivityAboutUsBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_about_us);
        setAppBar(mBinding.toolbar);
        mBinding.setClick(this);
        mBinding.ciVersion.setRightText(BuildConfig.VERSION_NAME);
        mBinding.ciOfficialWeb.setRightText(R.string.official_web);
    }

    @Override
    public void onClick(View v) {

    }
}
