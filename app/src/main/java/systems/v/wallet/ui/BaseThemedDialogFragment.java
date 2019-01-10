package systems.v.wallet.ui;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.wallet.Account;

public abstract class BaseThemedDialogFragment extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setDialogTheme();
        super.onCreate(savedInstanceState);
    }

    private void setDialogTheme() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        String publicKey = getArguments().getString("publicKey");
        if (TextUtils.isEmpty(publicKey)) {
            return;
        }
        Account account = App.getInstance().getWallet().getAccount(publicKey);
        if (account == null) {
            return;
        }
        if (account.isColdAccount()) {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_Large_Blue);
        } else {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_Large_Orange);
        }
    }
}
