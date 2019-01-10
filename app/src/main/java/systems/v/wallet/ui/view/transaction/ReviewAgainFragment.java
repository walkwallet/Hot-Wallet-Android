package systems.v.wallet.ui.view.transaction;

import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import systems.v.wallet.R;
import systems.v.wallet.basic.wallet.Transaction;

public class ReviewAgainFragment extends ReviewFragment {

    @Override
    protected Dialog createDialog(int animResId) {
        return super.createDialog(R.style.Animation_SlideRight);
    }

    public static class Builder extends ReviewFragment.Builder {
        public Builder(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @Override
        public ReviewFragment show(String publicKey, Transaction tx) {
            setTag("again");
            return super.show(publicKey, tx);
        }
    }
}
