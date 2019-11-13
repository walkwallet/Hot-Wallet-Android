package systems.v.wallet.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AssetJsonUtil {
    public static <T> T getJsonObj(Context context, String fileName, Class<T> cls){
        StringBuilder stringBuilder = new StringBuilder();
        AssetManager assetManager = context.getAssets();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName),"utf-8"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return JSON.parseObject(stringBuilder.toString(), cls);
    }
}
