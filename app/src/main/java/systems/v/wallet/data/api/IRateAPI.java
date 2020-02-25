package systems.v.wallet.data.api;

import io.reactivex.Observable;
import retrofit2.http.GET;
import systems.v.wallet.data.bean.RespBean;

public interface IRateAPI {
    @GET("/api/superNodesDetail")
    Observable<RespBean> getSupderNodeDetail();
}
