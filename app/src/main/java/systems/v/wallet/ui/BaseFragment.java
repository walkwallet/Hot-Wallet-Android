package systems.v.wallet.ui;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.Fragment;
import systems.v.wallet.utils.bus.AppBus;

public abstract class BaseFragment extends Fragment {

    protected Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
        AppBus.register(this);
    }

}
