package systems.v.wallet.data.api;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import systems.v.wallet.data.bean.RespBean;
import systems.v.wallet.data.bean.publicApi.TokenInfoBean;

public interface IPublicApi {
    @POST("/admin/apiToken")
    Observable<TokenInfoBean> apiToken(@Body Map<String, Object> request);

}
