package systems.v.wallet.data.api;

import java.util.Map;

import io.reactivex.Observable;
import systems.v.wallet.data.bean.publicApi.RespBean;


public class PublicApi implements IPublicApi{

    IPublicApi iPublicAPI;
    public PublicApi(IPublicApi api) {
        iPublicAPI = api;
    }

    @Override
    public Observable<RespBean> getTokenDetail(Map<String, Object> request) {
        return iPublicAPI.getTokenDetail(request);
    }
}
