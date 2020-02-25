package systems.v.wallet.data;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.data.api.IMainNetNodeAPI;
import systems.v.wallet.data.api.IPublicApi;
import systems.v.wallet.data.api.IRateAPI;
import systems.v.wallet.data.api.ITestNetNodeAPI;
import systems.v.wallet.data.api.NodeAPI;
import systems.v.wallet.data.api.PublicApi;
import systems.v.wallet.data.api.RateAPI;
import systems.v.wallet.data.converter.JsonConverterFactory;
import systems.v.wallet.utils.Constants;

public class RetrofitHelper {

    private static RetrofitHelper mInstance = new RetrofitHelper(false);

    public static RetrofitHelper getInstance() {
        return mInstance;
    }

    public static void setNetwork(String network) {
        mInstance = new RetrofitHelper(Wallet.TEST_NET.equals(network));
    }

    private NodeAPI mNodeAPI = null;
    private PublicApi mPublicAPI = null;
    private RateAPI mRateAPI = null;

    private RetrofitHelper(boolean isTest) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // timeout
        builder.connectTimeout(12, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        // log
        builder.addInterceptor(new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT));
        // retry
        builder.retryOnConnectionFailure(true);
        OkHttpClient client = builder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(isTest ? Constants.TEST_NET_API_SERVER : Constants.MAIN_NET_API_SERVER)
                .client(client)
                .addConverterFactory(JsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        IMainNetNodeAPI mainAPI = null;
        ITestNetNodeAPI testAPI = null;
        if (isTest) {
            testAPI = retrofit.create(ITestNetNodeAPI.class);
        } else {
            mainAPI = retrofit.create(IMainNetNodeAPI.class);
        }
        mNodeAPI = new NodeAPI(mainAPI, testAPI);

        Retrofit retrofitPublic = new Retrofit.Builder()
                .baseUrl(isTest ? Constants.PUBLIC_API_SERVER_TEST : Constants.PUBLIC_API_SERVER)
                .client(client)
                .addConverterFactory(JsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        mPublicAPI = new PublicApi(retrofitPublic.create(IPublicApi.class));

        Retrofit retrofitRate = new Retrofit.Builder()
                .baseUrl(isTest ? Constants.RATE_API_SERVER_TEST : Constants.RATE_API_SERVER)
                .client(client)
                .addConverterFactory(JsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        mRateAPI = new RateAPI(retrofitRate.create(IRateAPI.class));
    }

    public NodeAPI getNodeAPI() {
        return mNodeAPI;
    }

    public PublicApi getPublicAPI() {
        return mPublicAPI;
    }

    public RateAPI getRateAPI() {
        return mRateAPI;
    }

}
