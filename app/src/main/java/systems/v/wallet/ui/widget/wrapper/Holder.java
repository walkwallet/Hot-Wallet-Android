package systems.v.wallet.ui.widget.wrapper;

import android.view.View;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public class Holder<T extends ViewDataBinding> extends RecyclerView.ViewHolder {

    private T mViewDataBinding;

    public Holder(T viewDataBinding) {
        super(viewDataBinding.getRoot());
        this.mViewDataBinding = viewDataBinding;
    }

    public Holder(View itemView) {
        super(itemView);
    }

    public T getBinding() {
        return mViewDataBinding;
    }

}
