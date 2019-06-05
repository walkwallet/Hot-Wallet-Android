package systems.v.wallet.data;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.net.UnknownHostException;

import io.reactivex.functions.Consumer;
import retrofit2.HttpException;
import systems.v.wallet.data.bean.ErrorBean;

public class BaseErrorConsumer implements Consumer<Throwable> {
    private final static String TAG = "BaseErrorConsumer";
    private Callback cb;

    private BaseErrorConsumer(){}

    public static BaseErrorConsumer create(Callback cb){
        BaseErrorConsumer bec = new BaseErrorConsumer();
        bec.cb = cb;
        return bec;
    }

    @Override
    public void accept(Throwable throwable){
        Log.e(TAG, throwable.getClass().toString());
        if (throwable instanceof HttpException) {
            try {
                HttpException he = (HttpException) throwable;
                String body = he.response().errorBody().string();
                ErrorBean error = JSON.parseObject(body, ErrorBean.class);
                if(cb != null){
                    cb.onError(error.getError(), error.getMessage());
                }
            } catch (Exception e) {
                if(cb != null){
                    cb.onError(-1, "Resp error parse error");
                }
            }
        }else if(throwable instanceof UnknownHostException){
            cb.onError(-2, "Network connection failed!");
        }else{
            cb.onError(-3, throwable.getMessage());
        }
    }

    public interface Callback{
        void onError(int code, String msg);
    }
}
