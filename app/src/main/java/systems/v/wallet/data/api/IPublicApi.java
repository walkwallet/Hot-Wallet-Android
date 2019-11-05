package systems.v.wallet.data.api;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;
import systems.v.wallet.data.bean.publicApi.RespBean;

public interface IPublicApi {
    @POST("/api/getTokenDetail")
    Observable<RespBean> getTokenDetail(@Body Map<String, Object> request);

    @POST("/api/getTokenList")
    Observable<RespBean> getTokenList();
}
