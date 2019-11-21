package systems.v.wallet.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipUtil {
    public static String getClip(Context context){
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if(cm != null){
            ClipData clipData = cm.getPrimaryClip();
            if (cm.hasPrimaryClip() && clipData != null && clipData.getItemCount() > 0) {
                ClipData.Item item = clipData.getItemAt(0);
                return item.getText().toString();
            }
        }
        return "";
    }
}
