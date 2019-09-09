package systems.v.wallet.data.converter;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import systems.v.wallet.data.bean.ErrorBean;
import systems.v.wallet.data.bean.RespBean;

class JsonResponseBodyConverter implements Converter<ResponseBody, RespBean> {

    @Override
    public RespBean convert(ResponseBody responseBody) throws IOException {
        String responseString = responseBody.string();
        RespBean resp = new RespBean();
        resp.setCode(0);
        resp.setMsg("");
        resp.setData(responseString);
        responseBody.close();
        return resp;
    }
}