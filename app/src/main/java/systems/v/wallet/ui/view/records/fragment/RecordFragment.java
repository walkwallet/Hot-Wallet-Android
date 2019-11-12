package systems.v.wallet.ui.view.records.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.bean.RecordBean;
import systems.v.wallet.data.bean.RecordRespBean;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.statics.TokenHelper;
import systems.v.wallet.databinding.FragmentRecordBinding;
import systems.v.wallet.entity.RecordEntity;
import systems.v.wallet.ui.BaseActivity;
import systems.v.wallet.ui.BaseFragment;
import systems.v.wallet.ui.view.detail.DetailActivity;
import systems.v.wallet.ui.view.detail.adapter.RecordAdapter;
import systems.v.wallet.ui.view.records.DateSelectActivity;
import systems.v.wallet.ui.view.records.TransactionDetailActivity;
import systems.v.wallet.ui.view.records.TransactionRecordsActivity;
import systems.v.wallet.ui.widget.wrapper.BaseAdapter;
import systems.v.wallet.utils.DataUtil;
import systems.v.wallet.utils.DateUtils;
import systems.v.wallet.utils.LogUtil;

public class RecordFragment extends BaseFragment {
    public static final int TYPE_ALL = -1;
    public static final int TYPE_PAYMENT = 2;
    public static final int TYPE_LEASE = 3;
    public static final int TYPE_LEASE_OUT = 4;
    public static final int TYPE_CREATE_CONTRACT = 8;
    public static final int TYPE_EXECUTE_CONTRACT = 9;
    private final int PAGE_SIZE = 20;
    private int mPageNum = 0;
    private boolean isLoaded = false;
    private boolean isInitial = false;

    public static RecordFragment newInstance(String publicKey, int type) {
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        bundle.putString("publicKey", publicKey);
        RecordFragment recordFragment = new RecordFragment();
        recordFragment.setArguments(bundle);
        return recordFragment;
    }

    private FragmentRecordBinding mBinding;
    private RecordAdapter mAdapter;
    private List<RecordEntity> mData = new ArrayList<>();
    private List<RecordEntity> mRealData = new ArrayList<>();
    private int mType = TYPE_ALL;
    private Account mAccount;
    private boolean isAllLoaded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mType = getArguments().getInt("type", TYPE_ALL);
        String publicKey = getArguments().getString("publicKey");
        mAccount = App.getInstance().getWallet().getAccount(publicKey);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_record, null, false);
        mAdapter = new RecordAdapter(mRealData, mActivity);
        mAdapter.setHasHeader(false);
        mBinding.rvRecords.setLayoutManager(new LinearLayoutManager(mActivity));
        mBinding.rvRecords.setAdapter(mAdapter);
        mBinding.rvRecords.setOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean toBottom = false;
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = manager.getItemCount();

                    if (lastVisibleItem == (totalItemCount - 1) && toBottom && !isAllLoaded) {
                        mPageNum++;
                        getRecords();
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    toBottom = true;
                } else {
                    toBottom = false;
                }
            }
        });
        mAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                if (position >= 0 && position <= mRealData.size() - 1) {
                    RecordEntity recordEntity = mRealData.get(position);
                    TransactionDetailActivity.launch(mActivity, mAccount.getPublicKey(),
                            recordEntity, DataUtil.isCancelable(mData, recordEntity));
                }
            }
        });

        isInitial = true;

        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(isVisible() && !isLoaded) {
            initData();
        }
    }

    public void initData(){
        mPageNum = 0;
        getRecords();
    }

    public void getNoFilterData() {
        mRealData.clear();
        mRealData.addAll(mData);
    }

    public void getLastMonth() {
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (2592000000L);
        getRangeData(startTime, endTime);
    }

    public void getThisMonth() {
        long endTime = System.currentTimeMillis();
        String startTimeyM = DateUtils.formatyM(endTime);
        String startTimeYMD = startTimeyM + "-01";
        long startTime = DateUtils.StringDateToLong(startTimeYMD);
        getRangeData(startTime, endTime);
    }

    public void getThisYear() {
        long endTime = System.currentTimeMillis();
        String startTimeY = DateUtils.formatY(endTime);
        String startTimeYMD = startTimeY + "-01-01";
        long startTime = DateUtils.StringDateToLong(startTimeYMD);
        getRangeData(startTime, endTime);
    }

    public void getRangeData(long startTime, long endTime) {
        List<RecordEntity> list = new ArrayList<>();
        for (RecordEntity bean : mData) {
            if (startTime < bean.getFormatTimestamp() && bean.getFormatTimestamp() < endTime) {
                list.add(bean);
            }
        }
        mRealData.clear();
        mRealData.addAll(list);
    }

    public void changeData() {
        DateSelectActivity.Result result = ((TransactionRecordsActivity)mActivity).mFilterType;
        switch (result.getType()) {
            case DateSelectActivity.TYPE_NO_FILTER:
                getNoFilterData();
                break;
            case DateSelectActivity.TYPE_LAST_MONTH:
                getLastMonth();
                break;
            case DateSelectActivity.TYPE_THIS_MONTH:
                getThisMonth();
                break;
            case DateSelectActivity.TYPE_THIS_YEAR:
                getThisYear();
                break;
            case DateSelectActivity.TYPE_RANGE:
                long startTime, endTime;
                if (TextUtils.isEmpty(result.getStartTime())) {
                    startTime = DateUtils.StringDateToLong("1970-01-02");
                } else {
                    startTime = DateUtils.StringDateToLong(result.getStartTime());
                }
                if (TextUtils.isEmpty(result.getEndTime())) {
                    endTime = System.currentTimeMillis();
                } else {
                    endTime = DateUtils.StringDateToLong(result.getEndTime());
                }
                getRangeData(startTime, endTime);
                break;
        }
        mAdapter.notifyDataSetChanged();
    }

    private void getRecords() {
        isLoaded = true;
        Disposable d = RetrofitHelper.getInstance().getNodeAPI().records(mAccount.getAddress(), mType, PAGE_SIZE, mPageNum * PAGE_SIZE)
//                .compose(((BaseActivity)mActivity).<RespBean>bindLoading())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RespBean>() {
                    @Override
                    public void accept(RespBean respBean) throws Exception {
                        RecordRespBean resp = JSON.parseObject(respBean.getData(), RecordRespBean.class);
                        if (resp.getTransactions() != null && resp.getTransactions().size() > 0){
                            List<RecordBean> list = resp.getTransactions();
                            List<RecordEntity> recordEntityList = new ArrayList<>();
                            List<Token> verifiedToken = TokenHelper.getAddedVerifiedTokens(mActivity, mAccount.getPublicKey());
                            for (int i = 0; i < list.size(); i++) {
                                RecordBean bean = list.get(i);
                                RecordEntity entity = new RecordEntity(bean, verifiedToken, mAccount.getAddress());

                                if (entity.getRecordType() != RecordEntity.TYPE_NONE) {
                                    recordEntityList.add(entity);
                                }
                                //add one more tx when sent to self
                                if (entity.getRecordType() == RecordEntity.TYPE_RECEIVED &&
                                        entity.getSender().equals(entity.getRecipient())){
                                    RecordEntity entitySend = new RecordEntity(bean, verifiedToken, mAccount.getAddress());
                                    entitySend.setRecordType(RecordEntity.TYPE_SENT);
                                    recordEntityList.add(entitySend);
                                }
                            }
                            if(mPageNum == 0){
                                mData.clear();
                            }
                            mData.addAll(recordEntityList);
                            changeData();
                        }else{
                            isAllLoaded = true;
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.Log("err", throwable.toString());
                    }
                });
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && !isLoaded && isInitial){
            initData();
        }
    }
}
