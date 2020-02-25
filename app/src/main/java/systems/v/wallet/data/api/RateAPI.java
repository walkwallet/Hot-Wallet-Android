package systems.v.wallet.data.api;

import io.reactivex.Observable;
import systems.v.wallet.data.bean.RespBean;

public class RateAPI implements IRateAPI {

    IRateAPI iRateAPI;
    public RateAPI(IRateAPI api) {
        iRateAPI = api;
    }

    @Override
    public Observable<RespBean> getSupderNodeDetail() {
        return iRateAPI.getSupderNodeDetail();
    }
}
