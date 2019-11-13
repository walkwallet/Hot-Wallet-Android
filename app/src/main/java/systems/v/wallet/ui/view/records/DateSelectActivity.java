package systems.v.wallet.ui.view.records;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import java.util.Calendar;
import java.util.Locale;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.databinding.ActivityDateSelectBinding;
import systems.v.wallet.ui.BaseThemedActivity;
import systems.v.wallet.utils.DateUtils;

public class DateSelectActivity extends BaseThemedActivity implements View.OnClickListener {

    public static void launch(Activity from, int requestCode, String publicKey) {
        Intent intent = new Intent(from, DateSelectActivity.class);
        intent.putExtra("publicKey", publicKey);
        from.startActivityForResult(intent, requestCode);
        openAnimHorizontal(from);
    }

    ActivityDateSelectBinding mBinding;
    private Calendar mCalendar;
    private int mYear, mMonth, mDay;
    public static final int
            TYPE_NO_FILTER = 1,
            TYPE_LAST_MONTH = 2,
            TYPE_THIS_MONTH = 3,
            TYPE_THIS_YEAR = 4,
            TYPE_RANGE = 5;
    private int mNormalColor, mSelectColor;
    private int mType = TYPE_NO_FILTER;
    private String mExtra;
    private Drawable mSelectedDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.activity_date_select, null, false);
        mBinding.setClick(this);
        setAppBar(mBinding.toolbar);
        mCalendar = Calendar.getInstance(Locale.getDefault());
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH);
        mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mNormalColor = ContextCompat.getColor(this, R.color.text_strong);
        if (mAccount.isColdAccount()) {
            mSelectColor = ContextCompat.getColor(this, R.color.blue);
            mSelectedDrawable = ContextCompat.getDrawable(this, R.drawable.ico_selected_blue);
        } else {
            mSelectColor = ContextCompat.getColor(this, R.color.color_orange_strong);
            mSelectedDrawable = ContextCompat.getDrawable(this, R.drawable.ico_selected);
        }

        setContentView(mBinding.getRoot());
        initView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_time_start:
                getDate(mBinding.tvTimeStart);
                break;
            case R.id.ll_time_end:
                getDate(mBinding.tvTimeEnd);
                break;
            case R.id.ci_no_filter:
                setSelect(TYPE_NO_FILTER);
                break;
            case R.id.ci_last_month:
                setSelect(TYPE_LAST_MONTH);
                break;
            case R.id.ci_this_month:
                setSelect(TYPE_THIS_MONTH);
                break;
            case R.id.ci_this_year:
                setSelect(TYPE_THIS_YEAR);
                break;
            case R.id.btn_complete:
                Intent intent = new Intent();
                Result result = new Result();
                result.setType(mType);
                String startTime = "", endTime = "";
                if (mType == TYPE_RANGE) {
                    String start = mBinding.tvTimeStart.getText().toString();
                    String end = mBinding.tvTimeEnd.getText().toString();
                    if (start.equals(mActivity.getString(R.string.date_default_format))) {
                        startTime = "";
                    } else {
                        startTime = start;
                    }
                    if (end.equals(mActivity.getString(R.string.date_default_format))) {
                        endTime = "";
                    } else {
                        endTime = end;
                    }
                }
                result.setStartTime(startTime);
                result.setEndTime(endTime);
                result.setExtra(mExtra);
                intent.putExtra("result", JSON.toJSONString(result));
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    private void initView() {
        mBinding.ciThisMonth.setLeftText(DateUtils.getMonth(System.currentTimeMillis()));
        mBinding.ciThisYear.setLeftText(DateUtils.formatY(System.currentTimeMillis()));
        setSelect(mType);
    }

    private void getDate(final TextView textView) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                //modifyTvBirthday.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                int month = monthOfYear + 1;
                String monthStr;
                if (month < 10) {
                    monthStr = "0" + month;
                } else {
                    monthStr = String.valueOf(month);
                }
                String dayStr;
                if (dayOfMonth < 10) {
                    dayStr = "0" + dayOfMonth;
                } else {
                    dayStr = String.valueOf(dayOfMonth);
                }
                String date = year + "-" + monthStr + "-" + dayStr;
                textView.setText(date);
                setSelect(TYPE_RANGE);
            }
        }, mYear, mMonth, mDay);
        DatePicker datePicker = datePickerDialog.getDatePicker();
        // transform
        long minTime = 0, maxTime = 0;
        minTime = DateUtils.StringDateToLong("2000-01-01");
        //maxTime = StringDateToLong(mYear + "-" + mMonth + "-" + mDay + 1);
        datePicker.setMinDate(minTime);
        datePicker.setMaxDate(System.currentTimeMillis() - 1000L);
        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);
        datePickerDialog.show();
    }

    private void setSelect(int type) {
        mType = type;
        mBinding.ciNoFilter.setLeftTextColor(mNormalColor);
        mBinding.ciLastMonth.setLeftTextColor(mNormalColor);
        mBinding.ciThisYear.setLeftTextColor(mNormalColor);
        mBinding.ciThisMonth.setLeftTextColor(mNormalColor);
        mBinding.ciNoFilter.setRightIcon(null);
        mBinding.ciLastMonth.setRightIcon(null);
        mBinding.ciThisYear.setRightIcon(null);
        mBinding.ciThisMonth.setRightIcon(null);
        switch (type) {
            case TYPE_NO_FILTER:
                mBinding.ciNoFilter.setLeftTextColor(mSelectColor);
                mBinding.ciNoFilter.setRightIcon(mSelectedDrawable);
                break;
            case TYPE_LAST_MONTH:
                mBinding.ciLastMonth.setLeftTextColor(mSelectColor);
                mExtra = mBinding.ciLastMonth.getLeftTextText();
                mBinding.ciLastMonth.setRightIcon(mSelectedDrawable);
                break;
            case TYPE_THIS_MONTH:
                mBinding.ciThisMonth.setLeftTextColor(mSelectColor);
                mExtra = mBinding.ciThisMonth.getLeftTextText();
                mBinding.ciThisMonth.setRightIcon(mSelectedDrawable);
                break;
            case TYPE_THIS_YEAR:
                mBinding.ciThisYear.setLeftTextColor(mSelectColor);
                mExtra = mBinding.ciThisYear.getLeftTextText();
                mBinding.ciThisYear.setRightIcon(mSelectedDrawable);
                break;
        }
    }

    public static class Result {
        private int type;
        private String startTime;
        private String endTime;
        private String extra;

        public Result() {
        }

        public Result(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }
    }
}
