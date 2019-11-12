package systems.v.wallet.data.statics;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.api.IPublicApi;
import systems.v.wallet.data.api.NodeAPI;
import systems.v.wallet.utils.AssetJsonUtil;
import systems.v.wallet.utils.Constants;
import systems.v.wallet.utils.SPUtils;

public class TokenHelper {

    public static final String VERIFIED_TOKEN_ = "VERIFIED_TOKEN_";
    /**
     *
     * @param c
     * @param network "M" or "T"
     * @return
     */
    public static List<Token> getVerifiedFromCache(Context c, String network) {
//        Map map = AssetJsonUtil.getJsonObj(c, "verified_token.json", Map.class);
        String tokenStr = SPUtils.getString(VERIFIED_TOKEN_ + network);
        JSONArray jsonArray = JSON.parseArray(tokenStr);
        return jsonArray != null ? jsonArray.toJavaList(Token.class): new ArrayList<Token>();
    }

    public static List<Token> getAddedVerifiedTokens(Context c, String publicKey){
        final String key = Constants.WATCHED_TOKEN.concat(publicKey);
        List<Token> addedTokens = JSON.parseArray(SPUtils.getString(key), Token.class);
        List<Token> verifiedTokens = new ArrayList<>();
        if(addedTokens != null) {
            for (Token token : addedTokens) {
                if (token.isVerified()) {
                    verifiedTokens.add(token);
                }
            }
        }
        return verifiedTokens;
    }
}
