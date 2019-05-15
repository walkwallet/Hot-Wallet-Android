package systems.v.wallet.data.api;

import java.util.Map;

import io.reactivex.Observable;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.bean.publicApi.TokenInfoBean;

public class PublicApi implements IPublicApi{

    IPublicApi iPublicAPI;
    public PublicApi(IPublicApi api) {
        iPublicAPI = api;
    }

    @Override
    public Observable<TokenInfoBean> apiToken(Map<String, Object> request) {
        return iPublicAPI.apiToken(request);
    }
}
