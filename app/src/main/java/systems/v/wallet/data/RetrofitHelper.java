package systems.v.wallet.data;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import systems.v.wallet.basic.wallet.Wallet;
import systems.v.wallet.data.api.IMainNetNodeAPI;
import systems.v.wallet.data.api.ITestNetNodeAPI;
import systems.v.wallet.data.api.NodeAPI;
import systems.v.wallet.data.converter.JsonConverterFactory;
import systems.v.wallet.utils.Constants;

public class RetrofitHelper {

    private static RetrofitHelper mInstance = new RetrofitHelper(false);

    public static RetrofitHelper getInstance() {
        return mInstance;
    }

    public static void setNetwork(String network) {
        mInstance = new RetrofitHelper(true);
    }

    private NodeAPI mNodeAPI = null;

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
                .baseUrl(isTest ? Constants.TEST_NET_API_SERVER2 : Constants.MAIN_NET_API_SERVER)
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
    }

    public NodeAPI getNodeAPI() {
        return mNodeAPI;
    }
}
