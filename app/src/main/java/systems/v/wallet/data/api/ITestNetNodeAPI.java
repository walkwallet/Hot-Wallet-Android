package systems.v.wallet.data.api;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import systems.v.wallet.data.bean.RespBean;

public interface ITestNetNodeAPI {

    @GET("transactions/address/{address}/limit/{limit}")
    Observable<RespBean> records(@Path("address") String address, @Path("limit") int limit);

    @GET("addresses/balance/details/{address}")
    Observable<RespBean> balance(@Path("address") String address);

    @POST("vsys/broadcast/payment")
    Observable<RespBean> payment(@Body Map<String, Object> payment);

    @POST("leasing/broadcast/lease")
    Observable<RespBean> lease(@Body Map<String, Object> lease);

    @POST("leasing/broadcast/cancel")
    Observable<RespBean> cancelLease(@Body Map<String, Object> cancel);
}
