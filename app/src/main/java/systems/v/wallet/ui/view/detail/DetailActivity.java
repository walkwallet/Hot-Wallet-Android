package systems.v.wallet.ui.view.detail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.bean.AccountBean;
import systems.v.wallet.data.bean.RecordBean;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.databinding.ActivityWalletDetailBinding;
import systems.v.wallet.databinding.HeaderDetailBinding;
import systems.v.wallet.entity.RecordEntity;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.contract.CreateTokenActivity;
import systems.v.wallet.ui.view.detail.adapter.RecordAdapter;
import systems.v.wallet.ui.view.records.TransactionDetailActivity;
import systems.v.wallet.ui.view.records.TransactionRecordsActivity;
import systems.v.wallet.ui.view.setting.AddressManagementDetailActivity;
import systems.v.wallet.ui.view.transaction.SendActivity;
import systems.v.wallet.ui.widget.wrapper.BaseAdapter;
import systems.v.wallet.ui.widget.wrapper.HeaderAndFooterWrapper;
import systems.v.wallet.utils.DataUtil;
import systems.v.wallet.utils.UIUtil;
import systems.v.wallet.utils.bus.AppEvent;
import systems.v.wallet.utils.bus.annotation.Subscribe;

public class DetailActivity extends BaseThemedActivity implements View.OnClickListener {

    public static void launch(Activity from, String publicKey) {
        Intent intent = new Intent(from, DetailActivity.class);
        intent.putExtra("publicKey", publicKey);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private final String TAG = DetailActivity.class.getSimpleName();
    private ActivityWalletDetailBinding mBinding;
    private HeaderDetailBinding mHeaderBinding;
    private HeaderAndFooterWrapper mAdapter;
    private List<RecordEntity> mData = new ArrayList<>();
    private boolean mIsFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreen();
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_detail);
        initView();
        getRecords(mAccount.getAddress(), true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            if (menu.getClass() == MenuBuilder.class) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsFirst && mAccount != null) {
            getBalance(mAccount.getAddress());
            getRecords(mAccount.getAddress(), false);
        }
        if (mIsFirst) {
            mIsFirst = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_address_manage:
                AddressManagementDetailActivity.launch(mActivity, mAccount.getPublicKey());
                break;
            case R.id.nav_copy_address:
                UIUtil.copyToClipboard(this, mAccount.getAddress());
                break;
            case R.id.nav_create_token:
                CreateTokenActivity.launch(this, mAccount.getPublicKey());
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_send:
                SendActivity.launchPayment(this, mAccount.getPublicKey());
                break;
            case R.id.ll_receive:
                ReceiveActivity.launch(this, mAccount.getPublicKey());
                break;
            case R.id.ll_lease:
                SendActivity.launchLease(this, mAccount.getPublicKey());
                break;
            case R.id.ll_records:
                TransactionRecordsActivity.launch(mActivity, mAccount.getPublicKey());
                break;
        }
    }

    private void initView() {
        setAppBar(mBinding.toolbar);
        mBinding.toolbar.setTitle(UIUtil.getMutatedAddress(mAccount.getAddress()));
        RecordAdapter innerAdapter = new RecordAdapter(mData, this);
        innerAdapter.setHasHeader(true);
        mAdapter = new HeaderAndFooterWrapper(innerAdapter);
        mBinding.rvTransactionRecords.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvTransactionRecords.setAdapter(mAdapter);
        mHeaderBinding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.header_detail, mBinding.rvTransactionRecords, false);
        mHeaderBinding.setClick(this);
        mAdapter.addHeaderView(mHeaderBinding.getRoot());
        if (mAccount.isColdAccount()) {
            fillStatusViewWithDrawable(R.drawable.bg_gradient_monitor_no_radius);
            mBinding.flTop.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_gradient_monitor_no_radius));
            mHeaderBinding.llHeader.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_gradient_monitor_no_radius));
        } else {
            fillStatusViewWithDrawable(R.drawable.bg_gradient_wallet_no_radius);
            mBinding.flTop.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_gradient_wallet_no_radius));
            mHeaderBinding.llHeader.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_gradient_wallet_no_radius));
        }
        setHeaderData();
        innerAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                if (position >= 0 && position < mData.size()) {
                    RecordEntity recordEntity = mData.get(position);
                    TransactionDetailActivity.launch(mActivity, mAccount.getPublicKey(),
                            recordEntity, DataUtil.isCancelable(mData, recordEntity));
                }
            }
        });
    }

    private void getRecords(final String address, boolean loading) {
        Observable<RespBean> observable;
        if (loading) {
            observable = RetrofitHelper.getInstance().getNodeAPI().records(address, 1000)
                    .compose(this.<RespBean>bindLoading());
        } else {
            observable = RetrofitHelper.getInstance().getNodeAPI().records(address, 1000);
        }
        Disposable d = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RespBean>() {
                    @Override
                    public void accept(RespBean respBean) throws Exception {
                        List<List<RecordBean>> resultList = JSON.parseObject(respBean.getData(),
                                new TypeReference<List<List<RecordBean>>>() {
                                });
                        if (resultList != null && resultList.size() > 0
                                && resultList.get(0) != null && resultList.get(0).size() > 0) {
                            List<RecordBean> list = resultList.get(0);
                            List<RecordEntity> recordEntityList = new ArrayList<>();
                            for (int i = 0; i < list.size(); i++) {
                                RecordEntity entity = new RecordEntity(list.get(i));
                                entity.setAddress(address);
                                if (entity.getRecordType() != RecordEntity.TYPE_NONE) {
                                    recordEntityList.add(entity);
                                }
                            }
                            mData.clear();
                            mData.addAll(recordEntityList);
                            handleDataChange();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.getMessage());
                    }
                });
    }

    private void handleDataChange() {
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onAppEvent(AppEvent event) {
        switch (event.getType()) {
            case COLD_REMOVE:
                finish();
                break;
        }
    }

    private void getBalance(String address) {
        Log.v("getbalance", address);
        Disposable d = RetrofitHelper.getInstance().getNodeAPI().balance(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RespBean>() {
                    @Override
                    public void accept(RespBean respBean) throws Exception {
                        Log.v("zhi", JSON.toJSONString(respBean));
                        Log.d(TAG, JSON.toJSONString(respBean));
                        AccountBean accountBean = JSON.parseObject(respBean.getData(), AccountBean.class);
                        mAccount.updateBalance(accountBean);
                        setHeaderData();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.getMessage());
                    }
                });
    }

    private void setHeaderData() {
        if (mAccount != null) {
            mHeaderBinding.tvAvailableBalance.setText(CoinUtil.formatWithUnit(mAccount.getAvailable()));
            mHeaderBinding.tvTotalBalance.setText(CoinUtil.formatWithUnit(mAccount.getRegular()));
            mHeaderBinding.tvLeasedOut.setText(CoinUtil.formatWithUnit(mAccount.getRegular() - mAccount.getAvailable()));
            mHeaderBinding.tvLeasedIn.setText(CoinUtil.formatWithUnit(mAccount.getEffective() - mAccount.getAvailable()));
        }
    }
}
