package systems.v.wallet.ui.view.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.R;
import systems.v.wallet.databinding.ActivityConfirmSeedBinding;
import systems.v.wallet.ui.BaseActivity;
import systems.v.wallet.utils.DisplayUtil;

public class ConfirmSeedActivity extends BaseActivity {
    public static void launch(Activity from, String seed) {
        Intent intent = new Intent(from, ConfirmSeedActivity.class);
        intent.putExtra("seed", seed);
        from.startActivity(intent);
        openAnimHorizontal(from);
    }

    private ActivityConfirmSeedBinding mBinding;
    private String mSeed;
    private String[] mWords;
    private String[] mSeedWords;
    private String[] mAddedWords;
    private int mCounter;
    private List<TextView> mInputTextList = new ArrayList<>();
    private List<TextView> mWordTextList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_confirm_seed);
        setAppBar(mBinding.toolbar);
        Intent intent = getIntent();
        mSeed = intent.getStringExtra("seed");
        mSeedWords = mSeed.split(" ");
        mWords = reshuffle(Arrays.copyOf(mSeedWords, mSeedWords.length));
        mCounter = -1;
        mAddedWords = new String[mSeedWords.length];
        initView();
        initListener();

    }

    @Override
    public void finish() {
        super.finish();
        closeAnimHorizontal(mActivity);
    }

    private void initView() {
        changeNextStyle(false);
        for (String mWord : mWords) {
            mBinding.flexWords.addView(createNewFlexItemTextView(mWord, false));
            mBinding.flexInput.addView(createNewFlexItemTextView("", true));
        }
    }

    private void initListener() {
        mBinding.tvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });
        mBinding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackupSuccessActivity.launch(mActivity);
                finish();
            }
        });
    }

    private String[] reshuffle(String[] words) {
        List<String> wordsList = Arrays.asList(words);
        Collections.shuffle(wordsList);
        return wordsList.toArray(new String[wordsList.size()]);
    }

    private void addWord(final TextView wordCard, final String word) {
        mWordTextList.add(wordCard);
        wordCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordCard.setEnabled(false);
                wordCard.setTextColor(ContextCompat.getColor(mActivity, R.color.text_weak));
                wordCard.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.bg_white_with_stroke_radius_6));
                mCounter++;
                mAddedWords[mCounter] = word;
                TextView tvInput = mInputTextList.get(mCounter);
                tvInput.setVisibility(View.VISIBLE);
                tvInput.setText(word);
                if (mAddedWords[mAddedWords.length - 1] != null) {
                    List<String> a = Arrays.asList(mSeedWords);
                    List<String> b = Arrays.asList(mAddedWords);
                    if (Arrays.equals(mAddedWords, mSeedWords)) {
                        changeNextStyle(true);
                    } else {
                        changeNextStyle(false);
                    }
                }

            }
        });
    }

    private void clear() {
        for (int i = 0; i < mInputTextList.size(); i++) {
            TextView input = mInputTextList.get(i);
            input.setVisibility(View.GONE);
            TextView wordCard = mWordTextList.get(i);
            wordCard.setEnabled(true);
            wordCard.setTextColor(ContextCompat.getColor(this, R.color.white));
            wordCard.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_orange_radius_6));
            mCounter = -1;
            mAddedWords = new String[mSeedWords.length];
        }
    }

    private void changeNextStyle(boolean enable) {
        mBinding.btnNext.setEnabled(enable);
    }

    private TextView createNewFlexItemTextView(final String text, boolean isInputWord) {
        final TextView textView = (TextView) LayoutInflater.from(mActivity).inflate(R.layout.textview, null, false);
        textView.setText(text);
        if (isInputWord) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.text_strong));
            textView.setBackgroundResource(R.drawable.bg_unable_radius_6);
            textView.setVisibility(View.GONE);
            mInputTextList.add(textView);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCounter--;
                    changeNextStyle(false);
                    String text = textView.getText().toString();
                    for (TextView textWord : mWordTextList) {
                        String word = textWord.getText().toString();
                        if (text.equals(word) && !textWord.isEnabled()) {
                            textWord.setEnabled(true);
                            textWord.setTextColor(ContextCompat.getColor(mActivity, R.color.text_white));
                            textWord.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.bg_orange_radius_6));
                            textView.setVisibility(View.GONE);
                            List<String> selectStr = new ArrayList<>();
                            for (TextView visibleView : mInputTextList) {
                                if (visibleView.getVisibility() == View.VISIBLE) {
                                    selectStr.add(visibleView.getText().toString());
                                }
                            }
                            for (int k = 0; k < mInputTextList.size(); k++) {
                                if (k < selectStr.size()) {
                                    mInputTextList.get(k).setVisibility(View.VISIBLE);
                                    mInputTextList.get(k).setText(selectStr.get(k));
                                } else {
                                    mInputTextList.get(k).setVisibility(View.GONE);
                                }
                            }
                            break;
                        }
                    }
                }
            });
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.text_white));
            textView.setBackgroundResource(R.drawable.bg_orange_radius_6);
            textView.setVisibility(View.VISIBLE);
            addWord(textView, text);
        }
        FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = DisplayUtil.dp2px(this, 8);
        int marginBottom = DisplayUtil.dp2px(this, 12);
        layoutParams.setMargins(0, 0, margin, marginBottom);
        textView.setLayoutParams(layoutParams);
        return textView;
    }
}
