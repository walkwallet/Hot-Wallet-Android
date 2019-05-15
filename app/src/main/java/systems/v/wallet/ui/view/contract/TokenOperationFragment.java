package systems.v.wallet.ui.view.contract;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import systems.v.wallet.R;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.ui.widget.BottomSheetFragment;
import systems.v.wallet.utils.DisplayUtil;

public class TokenOperationFragment extends BottomSheetFragment {
    private List<Operation> operations;
    private Token token;

    private TokenOperationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int operationHeight = DisplayUtil.dp2px(inflater.getContext(), 48);

        LinearLayout ll = new LinearLayout(inflater.getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.setPadding(DisplayUtil.dp2px(inflater.getContext(), 12), 0 ,DisplayUtil.dp2px(inflater.getContext(), 12), 0);
        ll.setLayoutParams(lp);
        ll.setOrientation(LinearLayout.VERTICAL);

        LinearLayout llop = new LinearLayout(inflater.getContext());
        llop.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        llop.setOrientation(LinearLayout.VERTICAL);
        llop.setBackgroundResource(R.drawable.bg_white_radius_8);

        TextView tvTitle = new TextView(inflater.getContext());
        tvTitle.setMinHeight(operationHeight);
        tvTitle.setGravity(Gravity.CENTER);

        tvTitle.setText(token.getName());

        llop.addView(tvTitle);
        addDivider(inflater.getContext(), llop);

        for (int i = 0; i < operations.size(); i++){
            TextView tvOperation = new TextView(inflater.getContext());
            tvOperation.setGravity(Gravity.CENTER);
            tvOperation.setMinHeight(operationHeight);
            tvOperation.setText(operations.get(i).Name);
            final Operation.OperationListener listener = operations.get(i).listener;
            tvOperation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        listener.onOperation(TokenOperationFragment.this);
                    }
                    dismiss();
                }
            });
            tvOperation.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            tvOperation.setTextColor(getResources().getColor(R.color.text_strong));
            llop.addView(tvOperation);
            if(i != operations.size() - 1){
                addDivider(inflater.getContext(), llop);
            }
        }

        ll.addView(llop);

        TextView tvCancel = new TextView(inflater.getContext());
        LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        cancelLp.setMargins(0, DisplayUtil.dp2px(inflater.getContext(), 12), 0, 0);
        tvCancel.setLayoutParams(cancelLp);
        tvCancel.setGravity(Gravity.CENTER);
        tvCancel.setMinHeight(operationHeight);
        tvCancel.setText(R.string.cancel);
        tvCancel.setBackgroundResource(R.drawable.bg_white_radius_8);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        tvCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvCancel.setTextColor(getResources().getColor(R.color.text_weak));

        ll.addView(tvCancel);

        return ll;
    }

    public void addDivider(Context context, ViewGroup vg){
        View v = new View(context);
        v.setBackgroundColor(getResources().getColor(R.color.divider));
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DisplayUtil.dp2px(context, 1)));
        vg.addView(v);
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public static class Builder{
        private List<Operation> operations;
        private Token token;

        public TokenOperationFragment create(){
           TokenOperationFragment t = new TokenOperationFragment();
           t.operations = operations;
           t.token = token;

           return t;
        }

        public List<Operation> getOperations() {
            return operations;
        }

        public Builder setOperations(List<Operation> operations) {
            this.operations = operations;
            return this;
        }

        public Token getToken() {
            return token;
        }

        public Builder setToken(Token token) {
            this.token = token;
            return this;
        }
    }

    public static class Operation{
        public Operation(int name, OperationListener listener) {
            Name = name;
            this.listener = listener;
        }

        @IdRes
        public int Name;
        public OperationListener listener;

        public interface OperationListener{
            public void onOperation(TokenOperationFragment dialog);
        }
    }
}
