package systems.v.wallet.data.statics;

import android.content.Context;

import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.utils.AssetJsonUtil;

public class TokenHelper {

    /**
     *
     * @param c
     * @param network "M" or "T"
     * @return
     */
    public static List<Token> getVerifiedFromCache(Context c, String network) {
        Map map = AssetJsonUtil.getJsonObj(c, "verified_token.json", Map.class);
        JSONArray jsonArray = (JSONArray) map.get(network);
        return jsonArray != null ? jsonArray.toJavaList(Token.class): new ArrayList<Token>();
    }
}
