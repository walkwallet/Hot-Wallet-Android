package systems.v.wallet.utils;

import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import systems.v.wallet.App;
import systems.v.wallet.basic.utils.JsonUtil;

public class LogUtil {
    public static void Log(String tag, Object... args){
        Log.d(tag, JSON.toJSONString(args));
    }
}
