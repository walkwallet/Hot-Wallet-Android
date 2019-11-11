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

import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.R;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.bean.RecordBean;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.statics.TokenHelper;
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
    private List<RecordFragment> mFragments = new ArrayList<>();
    private @StringRes int[] txTypeStr = new int[]{R.string.records_all, R.string.detail_payment, R.string.detail_lease,
            R.string.detail_cancel_lease, R.string.detail_create_contract, R.string.detail_execute_contract};
    private int[] typeArray = new int[]{RecordFragment.TYPE_ALL, RecordFragment.TYPE_PAYMENT, RecordFragment.TYPE_LEASE,
            RecordFragment.TYPE_LEASE_OUT, RecordFragment.TYPE_CREATE_CONTRACT, RecordFragment.TYPE_EXECUTE_CONTRACT};
    public DateSelectActivity.Result mFilterType = new DateSelectActivity.Result(DateSelectActivity.TYPE_NO_FILTER);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_records);
        mBinding.setClick(this);
        setAppBar(mBinding.toolbar);
        initView();
    }

    private void initView() {
        String publicKey = mAccount.getPublicKey();
        for (int i=0; i < txTypeStr.length; i++ ){
            mTabs.add(getString(txTypeStr[i]));
            mFragments.add(RecordFragment.newInstance(publicKey, typeArray[i]));
        }
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
        mBinding.vpRecords.setOffscreenPageLimit(6);
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

    private void changeData(DateSelectActivity.Result result) {
        mFilterType = result;
        for (RecordFragment fragment : mFragments) {
            if (fragment != null) {
                fragment.changeData();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_date:
                DateSelectActivity.launch(mActivity, FILTER_SELECT_REQUEST_CODE, mAccount.getPublicKey());
                break;
            case R.id.iv_close_time:
                mBinding.flTime.setVisibility(View.GONE);
                changeData(new DateSelectActivity.Result(DateSelectActivity.TYPE_NO_FILTER));
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
