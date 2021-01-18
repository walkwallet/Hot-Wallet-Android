package systems.v.wallet.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class IntentUtil {

    public static void OpenLinkInBrowser(Context context, String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }
}
