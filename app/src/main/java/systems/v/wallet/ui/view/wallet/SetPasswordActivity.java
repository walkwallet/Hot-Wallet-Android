package systems.v.wallet.ui.view.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.databinding.ActivitySetPasswordBinding;
import systems.v.wallet.ui.BaseActivity;

public class SetPasswordActivity extends BaseActivity {
    public static void launch(Activity from, String seed) {
        Intent intent = new Intent(from, SetPasswordActivity.class);
        intent.putExtra("seed", seed);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    public static void launch(Activity from) {
        Intent intent = new Intent(from, SetPasswordActivity.class);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private static final String TAG = "SetPasswordActivity";

    private ActivitySetPasswordBinding mBinding;
    private int mRating;
    private View mPasswordStrength1;
    private View mPasswordStrength2;
    private View mPasswordStrength3;
    private View mPasswordStrength4;
    private View mPasswordStrength5;
    private TextView mPasswordStrengthTips;
    private int mColorStrength1, mColorStrength2, mColorStrength3,
            mColorStrength4, mColorStrength5, mBaseColor;
    private String mPassword;
    private String mSeed;
    private boolean mIsErrShowing = false;
    private boolean mRepeatErrShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_set_password);
        setAppBar((Toolbar) findViewById(R.id.toolbar));
        mSeed = getIntent().getStringExtra("seed");
        initView();
        initListener();
    }

    @Override
    public void finish() {
        super.finish();
        closeAnimHorizontal(mActivity);
    }

    private void initListener() {
        final Zxcvbn zxcvbn = new Zxcvbn();
        mBinding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            // Change password meter color and text according to password strength
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mIsErrShowing) {
                    mBinding.tvError.setVisibility(View.GONE);
                    mIsErrShowing = false;
                }
                if (mRepeatErrShowing) {
                    mBinding.tvConfirmError.setVisibility(View.GONE);
                    mRepeatErrShowing = false;
                }
                if (s.toString().length() >= 8) {
                    mBinding.btnComplete.setEnabled(true);
                } else {
                    mBinding.btnComplete.setEnabled(false);
                }
                Strength strength = zxcvbn.measure(s.toString());
                int strengthVal = strength.getScore() + 1;
                if (s.length() == 0) {
                    strengthVal = 0;
                }

                switch (strengthVal) {
                    case 0:
                        mRating = 0;
                        setStrength0();
                        break;

                    case 1:
                        mRating = 1;
                        setStrength1();
                        break;

                    case 2:
                        mRating = 2;
                        setStrength2();
                        break;

                    case 3:
                        mRating = 3;
                        setStrength3();
                        break;

                    case 4:
                        mRating = 4;
                        setStrength4();
                        break;

                    case 5:
                        mRating = 5;
                        setStrength5();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBinding.btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPassword = mBinding.etPassword.getText().toString();
                if (mPassword.length() < 8) {
                    mBinding.tvError.setVisibility(View.VISIBLE);
                    mIsErrShowing = true;
                    return;
                }
                String passwordRepeat = mBinding.etPasswordConfirm.getText().toString();
                if (mPassword.equals(passwordRepeat)) {
                    if (mRating <= 2) {
                        // weak password
                        TipsActivity.launch(mActivity, TipsActivity.TYPE_PASSWORD_WEAK, mSeed, mPassword);
                    } else {
                        // create
                        try {
                            TipsActivity.launch(mActivity, TipsActivity.TYPE_SUCCESS, mSeed, mPassword);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                } else {
                    //
                    mPassword = "";
                    mBinding.etPassword.setText("");
                    mBinding.etPassword.clearFocus();
                    mBinding.etPasswordConfirm.setText("");
                    mBinding.etPasswordConfirm.clearFocus();
                    mBinding.tvConfirmError.setVisibility(View.VISIBLE);
                    mRepeatErrShowing = true;
                }
            }
        });
    }

    private void initView() {
        mPasswordStrength1 = mBinding.viewWarn1;
        mPasswordStrength2 = mBinding.viewWarn2;
        mPasswordStrength3 = mBinding.viewWarn3;
        mPasswordStrength4 = mBinding.viewWarn4;
        mPasswordStrength5 = mBinding.viewWarn5;
        mPasswordStrengthTips = mBinding.tvPasswordWarning;
        mColorStrength1 = ContextCompat.getColor(this, R.color.passwordStrength1);
        mColorStrength2 = ContextCompat.getColor(this, R.color.passwordStrength2);
        mColorStrength3 = ContextCompat.getColor(this, R.color.passwordStrength3);
        mColorStrength4 = ContextCompat.getColor(this, R.color.passwordStrength4);
        mColorStrength5 = ContextCompat.getColor(this, R.color.passwordStrength5);
        mBaseColor = ContextCompat.getColor(this, R.color.text_weak);
        if (TextUtils.isEmpty(mSeed)) {
            mBinding.toolbar.setTitle(getString(R.string.wallet_create));
        } else {
            mBinding.toolbar.setTitle(getString(R.string.wallet_set_password));
        }
    }

    private void setStrength0() {
        mPasswordStrength1.setBackgroundColor(mBaseColor);
        mPasswordStrength2.setBackgroundColor(mBaseColor);
        mPasswordStrength3.setBackgroundColor(mBaseColor);
        mPasswordStrength4.setBackgroundColor(mBaseColor);
        mPasswordStrength5.setBackgroundColor(mBaseColor);

        mPasswordStrengthTips.setTextColor(mBaseColor);
        mPasswordStrengthTips.setText(R.string.password_strength_0);
    }

    private void setStrength1() {
        mPasswordStrength1.setBackgroundColor(mColorStrength1);
        mPasswordStrength2.setBackgroundColor(mBaseColor);
        mPasswordStrength3.setBackgroundColor(mBaseColor);
        mPasswordStrength4.setBackgroundColor(mBaseColor);
        mPasswordStrength5.setBackgroundColor(mBaseColor);

        mPasswordStrengthTips.setTextColor(mColorStrength1);
        mPasswordStrengthTips.setText(R.string.password_strength_1);
    }

    private void setStrength2() {
        mPasswordStrength1.setBackgroundColor(mColorStrength2);
        mPasswordStrength2.setBackgroundColor(mColorStrength2);
        mPasswordStrength3.setBackgroundColor(mBaseColor);
        mPasswordStrength4.setBackgroundColor(mBaseColor);
        mPasswordStrength5.setBackgroundColor(mBaseColor);

        mPasswordStrengthTips.setTextColor(mColorStrength2);
        mPasswordStrengthTips.setText(R.string.password_strength_2);
    }

    private void setStrength3() {
        mPasswordStrength1.setBackgroundColor(mColorStrength3);
        mPasswordStrength2.setBackgroundColor(mColorStrength3);
        mPasswordStrength3.setBackgroundColor(mColorStrength3);
        mPasswordStrength4.setBackgroundColor(mBaseColor);
        mPasswordStrength5.setBackgroundColor(mBaseColor);

        mPasswordStrengthTips.setTextColor(mColorStrength3);
        mPasswordStrengthTips.setText(R.string.password_strength_3);
    }

    private void setStrength4() {
        mPasswordStrength1.setBackgroundColor(mColorStrength4);
        mPasswordStrength2.setBackgroundColor(mColorStrength4);
        mPasswordStrength3.setBackgroundColor(mColorStrength4);
        mPasswordStrength4.setBackgroundColor(mColorStrength4);
        mPasswordStrength5.setBackgroundColor(mBaseColor);

        mPasswordStrengthTips.setTextColor(mColorStrength4);
        mPasswordStrengthTips.setText(R.string.password_strength_4);
    }

    private void setStrength5() {
        mPasswordStrength1.setBackgroundColor(mColorStrength5);
        mPasswordStrength2.setBackgroundColor(mColorStrength5);
        mPasswordStrength3.setBackgroundColor(mColorStrength5);
        mPasswordStrength4.setBackgroundColor(mColorStrength5);
        mPasswordStrength5.setBackgroundColor(mColorStrength5);

        mPasswordStrengthTips.setTextColor(mColorStrength5);
        mPasswordStrengthTips.setText(R.string.password_strength_5);
    }
}
