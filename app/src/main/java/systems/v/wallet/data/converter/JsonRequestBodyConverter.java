package systems.v.wallet.data.converter;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

final class JsonRequestBodyConverter<T> implements Converter<T, RequestBody> {
    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");

    JsonRequestBodyConverter() {
    }

    @Override
    public RequestBody convert(T value) throws IOException {
        try {
            String msg = JSON.toJSONString(value);
            Log.d("HTTP request", msg);
            return RequestBody.create(MEDIA_TYPE, msg);
        } catch (Exception e) {
            throw e;
        }
    }
}
