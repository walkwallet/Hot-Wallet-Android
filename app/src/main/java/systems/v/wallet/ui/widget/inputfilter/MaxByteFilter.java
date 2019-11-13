package systems.v.wallet.ui.widget.inputfilter;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

import systems.v.wallet.utils.LogUtil;

public class MaxByteFilter implements InputFilter {
    private final int DEFAULT_MAX = 140;
    private int maxByte = DEFAULT_MAX;

    public MaxByteFilter() {
    }

    public MaxByteFilter(int maxByte) {
        this.maxByte = maxByte;
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                               int dstart, int dend) {
        int keep = maxByte - (dest.toString().getBytes().length - dest.subSequence(dstart, dend).toString().getBytes().length);

        if (keep <= 0) {
            return "";
        } else if (keep >= source.subSequence(start, end).toString().getBytes().length) {
            return null; // keep original
        } else {
            for (int i=start; i < end;i++){
                CharSequence subChars = source.subSequence(start, i);
                if (subChars.toString().getBytes().length > keep ){
                    if (i - 1 > start) {
                        return subChars.subSequence(start, i - 1);
                    }else{
                        break;
                    }
                }
            }
            return "";
        }
    }
}
