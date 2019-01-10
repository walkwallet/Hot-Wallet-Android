package systems.v.wallet.ui.view.records;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.R;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.bean.RecordBean;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.databinding.ActivityTransactionRecordsBinding;
import systems.v.wallet.entity.RecordEntity;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.ui.view.records.fragment.RecordFragment;
import systems.v.wallet.utils.DateUtils;

public class TransactionRecordsActivity extends BaseThemedActivity implements View.OnClickListener {

    public static final int FILTER_SELECT_REQUEST_CODE = 1;

    public static void launch(Activity from, String publicKey) {
        Intent intent = new Intent(from, TransactionRecordsActivity.class);
        intent.putExtra("publicKey", publicKey);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private ActivityTransactionRecordsBinding mBinding;
    private List<String> mTabs = new ArrayList<>();
    private List<RecordEntity> mData = new ArrayList<>();
    private List<RecordFragment> mFragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_records);
        mBinding.setClick(this);
        setAppBar(mBinding.toolbar);
        initView();
    }

    private void initView() {
        mTabs.add(getString(R.string.records_all));
        mTabs.add(getString(R.string.detail_send));
        mTabs.add(getString(R.string.detail_receive));
        mTabs.add(getString(R.string.detail_lease));
        String publicKey = mAccount.getPublicKey();
        mFragments.add(RecordFragment.newInstance(publicKey, RecordFragment.TYPE_ALL));
        mFragments.add(RecordFragment.newInstance(publicKey, RecordFragment.TYPE_SEND));
        mFragments.add(RecordFragment.newInstance(publicKey, RecordFragment.TYPE_RECEIVE));
        mFragments.add(RecordFragment.newInstance(publicKey, RecordFragment.TYPE_LEASE));
        if (mAccount.isColdAccount()) {
            mBinding.llTime.setBackgroundResource(R.drawable.bg_blue_radius_13);
        } else {
            mBinding.llTime.setBackgroundResource(R.drawable.bg_orange_radius_13);
        }
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        };
        mBinding.vpRecords.setAdapter(adapter);
        for (String tab : mTabs) {
            mBinding.tlRecords.addTab(mBinding.tlRecords.newTab());
        }
        mBinding.tlRecords.setupWithViewPager(mBinding.vpRecords);
        for (int i = 0, len = mBinding.tlRecords.getTabCount(); i < len; i++) {
            mBinding.tlRecords.getTabAt(i).setText(mTabs.get(i));
        }
        mBinding.vpRecords.setOffscreenPageLimit(4);
        showLoading();
        getRecords(mAccount.getAddress());
    }

    private void getRecords(final String address) {
        Disposable d = RetrofitHelper.getInstance().getNodeAPI().records(address, 1000)
                .subscribeOn(Schedulers.io())
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
                            notifyDataChange();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == FILTER_SELECT_REQUEST_CODE) {
            String date = data.getStringExtra("result");
            DateSelectActivity.Result result = JSON.parseObject(date, DateSelectActivity.Result.class);
            showDate(result);
            changeData(result);
        }
    }

    private void notifyDataChange() {
        for (RecordFragment fragment : mFragments) {
            if (fragment != null) {
                fragment.notifyDataChange();
            }
        }
        hideLoading();
    }

    private void changeData(DateSelectActivity.Result result) {
        for (RecordFragment fragment : mFragments) {
            if (fragment != null) {
                fragment.changeData(result);
            }
        }
    }

    public List<RecordEntity> getData() {
        return mData;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_date:
                DateSelectActivity.launch(mActivity, FILTER_SELECT_REQUEST_CODE, mAccount.getPublicKey());
                break;
            case R.id.iv_close_time:
                mBinding.flTime.setVisibility(View.GONE);
                notifyDataChange();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void showDate(DateSelectActivity.Result result) {
        switch (result.getType()) {
            case DateSelectActivity.TYPE_NO_FILTER:
                mBinding.flTime.setVisibility(View.GONE);
                break;
            case DateSelectActivity.TYPE_THIS_MONTH:
            case DateSelectActivity.TYPE_LAST_MONTH:
            case DateSelectActivity.TYPE_THIS_YEAR:
                mBinding.tvTime.setText(result.getExtra());
                mBinding.flTime.setVisibility(View.VISIBLE);
                break;
            case DateSelectActivity.TYPE_RANGE:
                String start;
                String end;
                if (!TextUtils.isEmpty(result.getStartTime())) {
                    long startTime = DateUtils.StringDateToLong(result.getStartTime());
                    start = DateUtils.getMonthDayYear(startTime);
                } else {
                    start = "    ";
                }
                if (!TextUtils.isEmpty(result.getEndTime())) {
                    long endTime = DateUtils.StringDateToLong(result.getEndTime());
                    end = DateUtils.getMonthDayYear(endTime);
                } else {
                    end = "    ";
                }
                mBinding.tvTime.setText(start + " - " + end);
                mBinding.flTime.setVisibility(View.VISIBLE);
                break;
        }
    }
}
