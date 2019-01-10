package systems.v.wallet.ui;

import android.os.Bundle;

import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.wallet.Account;

public abstract class BaseThemedActivity extends BaseActivity {

    protected Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String publicKey = getIntent().getStringExtra("publicKey");
        mAccount = App.getInstance().getWallet().getAccount(publicKey);
        setActivityTheme(mAccount.isColdAccount());
        super.onCreate(savedInstanceState);
    }

    protected void setActivityTheme(boolean isColdAccount) {
        if (isColdAccount) {
            setTheme(R.style.AppTheme_Blue);
        } else {
            setTheme(R.style.AppTheme_Orange);
        }
    }
}
