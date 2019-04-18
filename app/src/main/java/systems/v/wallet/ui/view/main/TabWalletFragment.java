package systems.v.wallet.ui.view.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.FileUtil;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.bean.AccountBean;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.bean.TokenBean;
import systems.v.wallet.databinding.FooterAddWalletBinding;
import systems.v.wallet.databinding.FragmentTabWalletBinding;
import systems.v.wallet.ui.BaseFragment;
import systems.v.wallet.ui.view.detail.DetailActivity;
import systems.v.wallet.ui.view.main.adapter.WalletAdapter;
import systems.v.wallet.ui.widget.wrapper.BaseAdapter;
import systems.v.wallet.ui.widget.wrapper.HeaderAndFooterWrapper;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.DisplayUtil;
import systems.v.wallet.utils.SPUtils;
import systems.v.wallet.utils.bus.AppEvent;
import systems.v.wallet.utils.bus.annotation.Subscribe;

public class TabWalletFragment extends BaseFragment implements View.OnClickListener {
    public static final int TYPE_WALLET = 1;
    public static final int TYPE_MONITOR = 2;

    public static TabWalletFragment newInstance(int type) {
        TabWalletFragment fragment = new TabWalletFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    private final String TAG = TabWalletFragment.class.getSimpleName();

    private WalletAdapter mInnerAdapter;
    private HeaderAndFooterWrapper mAdapter;
    private List<Account> mData = new ArrayList<>();
    private FragmentTabWalletBinding mBinding;
    private FooterAddWalletBinding mFooterBinding;
    private Wallet mWallet;
    private List<Account> mAddressList = new ArrayList<>();
    private ArrayList<Account> mMonitorList = new ArrayList<>();
    private int mCounter = 0;
    private int mType = TYPE_WALLET;
    private Dialog mAddDialog;
    private Dialog mTipsDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_wallet, null, false);
        mInnerAdapter = new WalletAdapter(mData, mActivity);
        mAdapter = new HeaderAndFooterWrapper(mInnerAdapter);
        mType = getArguments().getInt("type");
        mInnerAdapter.setType(mType);
        mBinding.rvWallet.setLayoutManager(new LinearLayoutManager(mActivity));
        mBinding.rvWallet.setAdapter(mAdapter);
        mFooterBinding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.footer_add_wallet, mBinding.rvWallet, false);
        mFooterBinding.setClick(this);
        if (mType == TYPE_MONITOR) {
            mFooterBinding.tvAdd.setText(R.string.home_add_monitor);
            mFooterBinding.tvAddTips.setText(R.string.home_add_monitor_tips);
        } else {
            mFooterBinding.tvAdd.setText(R.string.home_add_address);
            mFooterBinding.tvAddTips.setText(R.string.home_add_address_tips);
        }
        mAdapter.addFootView(mFooterBinding.getRoot());
        initData();
        initListener();
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getData();
    }

    private void initData() {
        mWallet = App.getInstance().getWallet();
        if (mWallet == null) {
            return;
        }
        if (mType == TYPE_WALLET) {
            mFooterBinding.ivAdd.setImageResource(R.drawable.ico_add_wallet);
            mAddressList.clear();
            mAddressList.addAll(mWallet.getAccounts());
            mData.clear();
            for (int i = 0, len = mAddressList.size(); i < len; i++) {
                mData.add(mAddressList.get(i));
            }
            if (mData.size() == 0) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) mFooterBinding.getRoot().getLayoutParams();
                params.topMargin = DisplayUtil.dp2px(mActivity, 16f);
                mFooterBinding.getRoot().setLayoutParams(params);
            }
            mAdapter.notifyDataSetChanged();
        } else {
            mFooterBinding.ivAdd.setImageResource(R.drawable.ico_add_monitor);
            mMonitorList.clear();
            mMonitorList.addAll(mWallet.getColdAccounts());
            mData.clear();
            mData.addAll(mWallet.getColdAccounts());
            if (mData.size() == 0) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) mFooterBinding.getRoot().getLayoutParams();
                params.topMargin = DisplayUtil.dp2px(mActivity, 16f);
                mFooterBinding.getRoot().setLayoutParams(params);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private void initListener() {
        mInnerAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                if (position < mData.size()) {
                    DetailActivity.launch(mActivity, mData.get(position).getPublicKey());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_add:
                if (mType == TYPE_WALLET) {
                    // add wallet
                    addWallet();
                } else {
                    // add cold wallet
                    AddColdAccountActivity.launch(mActivity, MainActivity.ADD_COLD_ACCOUNT_REQUEST_CODE);
                }
                break;
        }
    }

    private void getBalance(String address, final int position) {
        Disposable d = RetrofitHelper.getInstance().getNodeAPI().balance(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RespBean>() {
                    @Override
                    public void accept(RespBean respBean) throws Exception {
                        Log.d(TAG, JSON.toJSONString(respBean));
                        AccountBean accountBean = JSON.parseObject(respBean.getData(), AccountBean.class);
                        onGetData(accountBean, position);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    private void onAdd(String address) {
        Disposable d = RetrofitHelper.getInstance().getNodeAPI().balance(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RespBean>() {
                    @Override
                    public void accept(RespBean respBean) throws Exception {
                        Log.d(TAG, JSON.toJSONString(respBean));
                        AccountBean accountBean = JSON.parseObject(respBean.getData(), AccountBean.class);
                        mData.get(mData.size() - 1).updateBalance(accountBean);
                        mAdapter.notifyDataSetChanged();

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    private void onGetData(AccountBean accountBean, int position) {
        mCounter++;
        Account account = mData.get(position);
        account.updateBalance(accountBean);
        if (mCounter == mData.size()) {
            mAdapter.notifyDataSetChanged();
            mCounter = 0;
        }
    }

    private void addWallet() {
        if (mAddDialog == null) {
            mAddDialog = new AlertDialog.Builder(mActivity).setIcon(R.drawable.ico_wallet_circle)
                    .setTitle(R.string.home_add_new)
                    .setMessage(R.string.home_add_new_tips)
                    .setPositiveButton(R.string.home_add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mWallet.append(1);
                            mAddressList.clear();
                            FileUtil.save(mActivity, mWallet);
                            mAddressList.addAll(mWallet.getAccounts());
                            mData.clear();
                            mData.addAll(mWallet.getAccounts());
                            mAdapter.notifyDataSetChanged();
                            try {
                                mBinding.rvWallet.getLayoutManager().scrollToPosition(mData.size());
                            } catch (Exception ignored) {

                            }
                            onAdd(mData.get(mData.size() - 1).getAddress());
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
        }
        mAddDialog.show();
    }

    @Subscribe
    public void onAppEvent(AppEvent event) {
        switch (event.getType()) {
            case COLD_ADD:
                if (mType == TYPE_MONITOR) {
                    if (mWallet == null) {
                        return;
                    }
                    mMonitorList.clear();
                    mMonitorList.addAll(mWallet.getColdAccounts());
                    mData.add(mMonitorList.get(mMonitorList.size() - 1));
                    mAdapter.notifyDataSetChanged();
                    try {
                        mBinding.rvWallet.getLayoutManager().scrollToPosition(mData.size());
                    } catch (Exception ignored) {

                    }
                    onAdd(mData.get(mData.size() - 1).getAddress());
                }
                break;
            case COLD_REMOVE:
                if (mType == TYPE_MONITOR) {
                    if (mWallet == null) {
                        return;
                    }
                    mMonitorList.clear();
                    mMonitorList.addAll(mWallet.getColdAccounts());
                    mData.clear();
                    mData.addAll(mWallet.getColdAccounts());
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    if (mMonitorList != null && mMonitorList.size() > 0) {
                        mCounter = 0;
                        for (int i = 0, len = mMonitorList.size(); i < len; i++) {
                            getBalance(mMonitorList.get(i).getAddress(), i);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            return;
        }
        if (mType == TYPE_MONITOR && SPUtils.getBoolean(Constants.FIRST_ENTER, true)) {
            if (mTipsDialog == null) {
                mTipsDialog = new AlertDialog.Builder(mActivity)
                        .setIcon(R.drawable.basic_ico_alert)
                        .setTitle(R.string.official_warning)
                        .setMessage(R.string.first_enter_tips)
                        .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SPUtils.setBoolean(Constants.FIRST_ENTER, false);
                                getData();
                            }
                        }).create();
            }
            mTipsDialog.show();
        } else {
            getData();
        }
    }

    public void getData() {
        if (mType == TYPE_WALLET) {
            if (mAddressList != null && mAddressList.size() > 0) {
                mCounter = 0;
                for (int i = 0, len = mAddressList.size(); i < len; i++) {
                    getBalance(mAddressList.get(i).getAddress(), i);
                }
            }
        } else {
            if (mMonitorList != null && mMonitorList.size() > 0) {
                mCounter = 0;
                for (int i = 0, len = mMonitorList.size(); i < len; i++) {
                    getBalance(mMonitorList.get(i).getAddress(), i);
                }
            }
        }
    }
}
