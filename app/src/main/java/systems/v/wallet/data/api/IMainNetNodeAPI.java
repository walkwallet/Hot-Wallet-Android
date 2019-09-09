package systems.v.wallet.data.api;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import systems.v.wallet.data.bean.RespBean;

public interface IMainNetNodeAPI {

    @GET("transactions/address/{address}/limit/{limit}")
    Observable<RespBean> records(@Path("address") String address, @Path("limit") int limit);

//    @GET("transactions/list")
//    Observable<RespBean> records(@Query("address") String address, @Query("txType") int txType,
//                                 @Query("limit") int limit, @Query("offset") int offset);
    @GET("transactions/list")
    Observable<RespBean> records(@Query("address") String address, @QueryMap Map<String, Integer> intParams);


    @GET("addresses/balance/details/{address}")
    Observable<RespBean> balance(@Path("address") String address);

    @POST("vsys/broadcast/payment")
    Observable<RespBean> payment(@Body Map<String, Object> payment);

    @POST("leasing/broadcast/lease")
    Observable<RespBean> lease(@Body Map<String, Object> lease);

    @POST("leasing/broadcast/cancel")
    Observable<RespBean> cancelLease(@Body Map<String, Object> cancel);

    @POST("contract/broadcast/register")
    Observable<RespBean> registerContract(@Body Map<String, Object> register);

    @POST("contract/broadcast/execute")
    Observable<RespBean> executeContract(@Body Map<String, Object> execute);

    @GET("contract/tokenInfo/{tokenId}")
    Observable<RespBean> tokenInfo(@Path("tokenId") String tokenId);

    @GET("contract/balance/{address}/{tokenId}")
    Observable<RespBean> tokenBalance(@Path("address") String address, @Path("tokenId") String tokenId);

    @GET("contract/content/{contractId}")
    Observable<RespBean> contractContent(@Path("contractId") String contractId);

    @GET("contract/info/{contractId}")
    Observable<RespBean> contractInfo(@Path("contractId") String contractId);
}
