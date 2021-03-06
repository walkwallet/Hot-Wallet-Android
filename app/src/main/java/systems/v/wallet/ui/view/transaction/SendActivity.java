package systems.v.wallet.ui.view.transaction;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.databinding.DataBindingUtil;

import com.alibaba.fastjson.JSON;
import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirPutCallback;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.JsonUtil;
import systems.v.wallet.basic.wallet.Operation;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.data.BaseErrorConsumer;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.api.RateAPI;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.bean.rateApi.SubNodeBean;
import systems.v.wallet.data.bean.rateApi.SuperNodeDetailsBean;
import systems.v.wallet.data.bean.rateApi.SuperNodeDetailsRespBean;
import systems.v.wallet.databinding.ActivitySendBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.transaction.adapter.SuperNodeDetailAdapter;
import systems.v.wallet.ui.view.transaction.adapter.SuperNodeInfo;
import systems.v.wallet.ui.widget.inputfilter.MaxByteFilter;
import systems.v.wallet.utils.ClipUtil;
import systems.v.wallet.utils.ToastUtil;
import systems.v.wallet.utils.UIUtil;
import vsys.Vsys;

public class SendActivity extends BaseThemedActivity implements View.OnClickListener {

    public static void launchPayment(Activity from, String publicKey) {
        launch(from, publicKey, Transaction.PAYMENT);
    }

    public static void launchLease(Activity from, String publicKey) {
        launch(from, publicKey, Transaction.LEASE);
    }

    public static void launch(Activity from, String publicKey, int type) {
        Intent intent = new Intent(from, SendActivity.class);
        intent.putExtra("publicKey", publicKey);
        intent.putExtra("type", type);
        from.startActivity(intent);
    }

    private static final String TAG = "SendActivity";

    private ActivitySendBinding mBinding;
    private Transaction mTransaction;
    private int mType;
    private ListPopupWindow mListPopupWindow;
    private long mFee = Transaction.DEFAULT_FEE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_send);
        mType = getIntent().getIntExtra("type", Transaction.PAYMENT);
        setAppBar(mBinding.toolbar);
        mBinding.setClick(this);

        // set balance and fee
        String balance = CoinUtil.formatWithUnit(mAccount.getAvailable());
        mBinding.tvAvailableBalance.setText(getString(R.string.send_available_balance, balance));
        String fee = CoinUtil.formatWithUnit(mFee);
        mBinding.tvFee.setText(fee);

        if (mType == Transaction.LEASE) {
            mBinding.toolbar.setTitle(R.string.send_lease_title);
            mBinding.llAttachment.setVisibility(View.GONE);
            mBinding.tvAmountLabel.setText(R.string.send_lease_amount);
            mBinding.tvSendToLabel.setText(R.string.send_lease_to);
            mBinding.etAddress.setHint(R.string.send_lease_address_input_hint);
            Drawable icoArrowDown = getResources().getDrawable(R.drawable.ico_arrow_down);
            icoArrowDown.setBounds(0, 0, icoArrowDown.getMinimumWidth(), icoArrowDown.getMinimumHeight());
            mBinding.etAddress.setCompoundDrawables(null, null, icoArrowDown, null);
            mListPopupWindow = new ListPopupWindow(this);
            mListPopupWindow.setAnchorView(mBinding.etAddress);
            mListPopupWindow.setModal(true);
            initSuperNodeData();
        } else {
            mBinding.toolbar.setTitle(R.string.send_payment_title);
            mBinding.flLeaseTips.setVisibility(View.GONE);
            mBinding.tvSendToLabel.setText(R.string.send_payment_to);
            mBinding.etAddress.setHint(R.string.send_address_input_hint);
        }
        mBinding.etAttachment.setFilters(new InputFilter[]{new MaxByteFilter()});
        initListener();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListener() {
        UIUtil.setAmountInputFilter(mBinding.etAmount);
        mBinding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                long value = CoinUtil.parse(s.toString());
                if (TextUtils.isEmpty(s) || (mAccount.getAvailable() - value) >= mFee) {
                    mBinding.tvBalanceError.setVisibility(View.GONE);
                } else {
                    mBinding.tvBalanceError.setText(getString(R.string.send_insufficient_balance_error, "VSYS"));
                    mBinding.tvBalanceError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBinding.etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s) || Wallet.validateAddress(s.toString())) {
                    mBinding.tvAddressError.setVisibility(View.GONE);
                } else {
                    mBinding.tvAddressError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (mType == Transaction.LEASE) {
            mListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    Drawable icoArrowDown = getResources().getDrawable(R.drawable.ico_arrow_down);
                    icoArrowDown.setBounds(0, 0, icoArrowDown.getMinimumWidth(), icoArrowDown.getMinimumHeight());
                    mBinding.etAddress.setCompoundDrawables(null, null, icoArrowDown, null);
                }
            });

            mBinding.etAddress.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    final int DRAWABLE_RIGHT = 2;
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (motionEvent.getX() >= (mBinding.etAddress.getWidth() - mBinding.etAddress
                                .getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            if (mListPopupWindow.isShowing()) {
                                mListPopupWindow.dismiss();
                            } else {
                                Drawable icoArrowUp = getResources().getDrawable(R.drawable.ico_arrow_up);
                                icoArrowUp.setBounds(0, 0, icoArrowUp.getMinimumWidth(), icoArrowUp.getMinimumHeight());
                                mBinding.etAddress.setCompoundDrawables(null, null, icoArrowUp, null);
                                mListPopupWindow.show();
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_max: {
                mBinding.etAmount.setText(CoinUtil.format(mAccount.getAvailable() - mFee));
            }
            break;
            case R.id.btn_paste: {
                mBinding.etAddress.setText(ClipUtil.getClip(this));
            }
            break;
            case R.id.btn_scan:
                ScannerActivity.launch(this);
                break;
            case R.id.btn_confirm: {
                String amount = mBinding.etAmount.getText().toString();
                String address = mBinding.etAddress.getText().toString();
                String str = null;
                if (TextUtils.isEmpty(amount)) {
                    str = getString(R.string.send_amount_empty_error);
                } else if ((mAccount.getAvailable() - CoinUtil.parse(amount)) < CoinUtil.parse(mBinding.tvFee.getText().toString(), (long)Math.pow(10, 8))) {
                    str = getString(R.string.send_insufficient_balance_error, "VSYS");
                } else if (!Wallet.validateAddress(address)) {
                    str = getString(R.string.send_address_input_error);
                }
                if (mType == Transaction.LEASE && address.equals(mAccount.getAddress())) {
                    str = getString(R.string.send_lease_to_self_error);
                }
                if (str != null) {
                    new AlertDialog.Builder(mActivity)
                            .setMessage(str)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                    return;
                }
                if (address.equals(mAccount.getAddress())) {
                    ToastUtil.showLongToast(R.string.send_to_self_error);
                }
                generateTransaction();
                ResultActivity.launch(this, mAccount.getPublicKey(), mTransaction);
            }
            break;
            case R.id.btn_explain: {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://vsysrate.com/wiki/vsys-coin-leasing.html"));
                mActivity.startActivity(intent);
            }
            break;
            case R.id.btn_supernode_list: {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://vsysrate.com"));
                mActivity.startActivity(intent);
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String qrContents = result.getContents();
            if (!TextUtils.isEmpty(qrContents)) {
                // scan receive address
                Operation op = null;
                if (JsonUtil.isJsonString(qrContents)) {
                    op = Operation.parse(qrContents);
                } else if (Wallet.validateAddress(qrContents)) {
                    op = new Operation(Operation.ACCOUNT);
                    op.set("address", qrContents);
                }
                if (op == null || !op.validate(Operation.ACCOUNT)) {
                    Log.e(TAG, "scan result is not an account opc");
                    UIUtil.showUnsupportQrCodeDialog(this);
                    return;
                }

                if (op.get("address") != null) {
                    String address = op.getString("address");
                    if(!mWallet.getNetwork().equals(Vsys.getNetworkFromAddress(address))) {
                        if (mWallet.getNetwork().equals(Vsys.NetworkMainnet)) {
                            UIUtil.showInconsistentNetworkMainnetDialog(this);
                        }else {
                            UIUtil.showInconsistentNetworkTestnetDialog(this);
                        }
                        return;
                    }
                    mBinding.etAddress.setText(address);
                }
                if (op.get("amount") != null) {
                    long amount = op.getLong("amount");
                    String text = null;
                    if (amount != 0) {
                        text = CoinUtil.format(amount);
                    }
                    mBinding.etAmount.setText(text);
                }
                if (op.get("invoice") != null) {
                    String invoice = op.getString("invoice");
                    mBinding.etAttachment.setText(invoice);
                }
            }
        }
    }

    private void generateTransaction() {
        mTransaction = new Transaction();
        mTransaction.setTransactionType(mType);
        mTransaction.setSenderPublicKey(mAccount.getPublicKey());
        mTransaction.setAmount(CoinUtil.parse(mBinding.etAmount.getText().toString()));
        mTransaction.setRecipient(mBinding.etAddress.getText().toString());
        mTransaction.setAttachment(mBinding.etAttachment.getText().toString());
        mTransaction.setTimestamp(System.currentTimeMillis());
        mTransaction.setFee(CoinUtil.parse(mBinding.tvFee.getText().toString(), (long)Math.pow(10, 8)));
    }

    private void initSuperNodeData() {
        if(mType == Transaction.LEASE) {
            final RateAPI rateAPI = RetrofitHelper.getInstance().getRateAPI();
            Observable<RespBean> obser = rateAPI.getSupderNodeDetail();
            if (getSuperNodeInfoList().size() != 0) {
                updateSuperNodeInfoAdapter(getSuperNodeInfoList());
            } else {
                obser = obser.compose(this.<RespBean>bindLoading());
                try {
                    Reservoir.init(this, 1024 * 10); //in bytes
                } catch (IOException e) {
                    //failure
                    obser = obser.compose(this.<RespBean>bindLoading());
                }
            }
            Disposable d = obser.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<RespBean>() {
                        @Override
                        public void accept(RespBean respBean) throws Exception {
                            SuperNodeDetailsRespBean resp = JSON.parseObject(respBean.getData(), SuperNodeDetailsRespBean.class);
                            if (resp != null && resp.getCode() == 0) {
                                final List<SuperNodeInfo> superNodeInfoList = new ArrayList<SuperNodeInfo>();
                                for (SuperNodeDetailsBean nodeDetail : resp.getData()) {
                                    if (!nodeDetail.isSuperNode()) {
                                        continue;
                                    }
                                    SuperNodeInfo superNode = new SuperNodeInfo(nodeDetail.getName(), nodeDetail.getAddress());
                                    superNodeInfoList.add(superNode);
                                    if (nodeDetail.getSubNode() != null) {
                                        for (SubNodeBean one : nodeDetail.getSubNode()) {
                                            SuperNodeInfo subNode = new SuperNodeInfo(nodeDetail.getName(), nodeDetail.getAddress(), one.getName(), one.getId());
                                            superNodeInfoList.add(subNode);
                                        }
                                    }
                                }
                                setSuperNodeInfoList(superNodeInfoList);
                            }
                        }
                    }, BaseErrorConsumer.create(new BaseErrorConsumer.Callback() {
                        @Override
                        public void onError(int code, String msg) {
                            mListPopupWindow.setWidth(mBinding.etAddress.getWidth());
                            mListPopupWindow.setHeight(500);
                        }
                    }));
        }
    }

    private List<SuperNodeInfo> getSuperNodeInfoList() {
        Type resultType = new TypeToken<List<SuperNodeInfo>>() {}.getType();
        try {
           return Reservoir.get("superNodeInfoList", resultType);
        } catch (Exception e) {
            return new ArrayList<SuperNodeInfo>();
        }
    }

    private void setSuperNodeInfoList(final List<SuperNodeInfo> superNodeInfoList) {
        updateSuperNodeInfoAdapter(superNodeInfoList);
        try {
            Reservoir.putAsync("superNodeInfoList", superNodeInfoList, new ReservoirPutCallback() {
                @Override
                public void onSuccess() { }

                @Override
                public void onFailure(Exception e) { }
            });
        }catch (Exception e) {
        }
    }

    private void updateSuperNodeInfoAdapter(final List<SuperNodeInfo> superNodeInfoList){
        int mListPopupWindowWidth;
        if(mBinding.etAddress.getWidth() != 0){
            mListPopupWindowWidth = mBinding.etAddress.getWidth();
        }else {
            int deviceWidth;
            WindowManager wm = (WindowManager) mBinding.etAddress.getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                Point size = new Point();
                display.getSize(size);
                deviceWidth = size.x;
            } else {
                deviceWidth = display.getWidth();
            }
            int padding = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
            mListPopupWindowWidth = deviceWidth-padding*2;
        }
        mListPopupWindow.setAdapter(new SuperNodeDetailAdapter(SendActivity.this, superNodeInfoList));
        mListPopupWindow.setWidth(mListPopupWindowWidth);
        mListPopupWindow.setHeight(500);

        mListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SuperNodeInfo nodeInfo = superNodeInfoList.get(i);

                mBinding.etAddress.setText(nodeInfo.address);
                String fee = CoinUtil.formatWithUnit(mFee);
                if (nodeInfo.isSubNode) {
                    mFee = Transaction.DEFAULT_FEE + nodeInfo.subNodeId;
                    fee = CoinUtil.formatWithUnit(mFee);
                }
                mBinding.tvFee.setText(fee);

                if (mListPopupWindow.isShowing()) {
                    mListPopupWindow.dismiss();
                }
            }
        });
    }

}
