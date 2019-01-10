package systems.v.wallet.ui.view.records.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.databinding.FragmentRecordBinding;
import systems.v.wallet.entity.RecordEntity;
import systems.v.wallet.ui.BaseFragment;
import systems.v.wallet.ui.view.detail.adapter.RecordAdapter;
import systems.v.wallet.ui.view.records.DateSelectActivity;
import systems.v.wallet.ui.view.records.TransactionDetailActivity;
import systems.v.wallet.ui.view.records.TransactionRecordsActivity;
import systems.v.wallet.ui.widget.wrapper.BaseAdapter;
import systems.v.wallet.utils.DataUtil;
import systems.v.wallet.utils.DateUtils;

public class RecordFragment extends BaseFragment {
    public static final int TYPE_ALL = 1;
    public static final int TYPE_SEND = 2;
    public static final int TYPE_RECEIVE = 3;
    public static final int TYPE_LEASE = 4;

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
        return mBinding.getRoot();
    }

    public void notifyDataChange() {
        if (mActivity != null) {
            mData.clear();
            mData.addAll(((TransactionRecordsActivity) mActivity).getData());
            mRealData.clear();
            getRealData(mType);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void getNoFilterData() {
        mRealData.clear();
        mRealData.addAll(mData);
    }

    public void getLastMonth() {
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (25920000000L);
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
        mRealData.clear();
        mRealData = getRealData(mType);
        List<RecordEntity> list = new ArrayList<>();
        for (RecordEntity bean : mRealData) {
            if (startTime < bean.getFormatTimestamp() && bean.getFormatTimestamp() < endTime) {
                list.add(bean);
            }
        }
        mRealData.clear();
        mRealData.addAll(list);
    }

    public void changeData(DateSelectActivity.Result result) {
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

    public void getAll() {
        mRealData.clear();
        mRealData.addAll(mData);
    }

    public void getSend() {
        mRealData.clear();
        for (RecordEntity entity : mData) {
            if (entity.getRecordType() == RecordEntity.TYPE_SENT) {
                mRealData.add(entity);
            }
        }
    }

    public void getReceive() {
        mRealData.clear();
        for (RecordEntity entity : mData) {
            if (entity.getRecordType() == RecordEntity.TYPE_RECEIVED
                    || entity.getRecordType() == RecordEntity.TYPE_MINTING) {
                mRealData.add(entity);
            }
        }
    }

    public void getLease() {
        mRealData.clear();
        for (RecordEntity entity : mData) {
            if (entity.getRecordType() == RecordEntity.TYPE_CANCELED_OUT_LEASING
                    || entity.getRecordType() == RecordEntity.TYPE_CANCELED_IN_LEASING
                    || entity.getRecordType() == RecordEntity.TYPE_START_OUT_LEASING
                    || entity.getRecordType() == RecordEntity.TYPE_START_IN_LEASING) {
                mRealData.add(entity);
            }
        }
    }

    public List<RecordEntity> getRealData(int type) {
        switch (type) {
            case RecordFragment.TYPE_ALL:
                getAll();
                break;
            case RecordFragment.TYPE_SEND:
                getSend();
                break;
            case RecordFragment.TYPE_LEASE:
                getLease();
                break;
            case RecordFragment.TYPE_RECEIVE:
                getReceive();
                break;

        }
        return mRealData;
    }
}
