package systems.v.wallet.data.converter;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import systems.v.wallet.data.bean.publicApi.RespBean;

class JsonResponseBodyConverter2 implements Converter<ResponseBody, RespBean> {

    @Override
    public RespBean convert(ResponseBody responseBody) throws IOException {
        String responseString = responseBody.string();
        try {
            RespBean resp = JSON.parseObject(responseString, RespBean.class);
            if (resp.getCode() != 0) {
                resp.setData(responseString);
            }else{
                resp.setData(resp.getData().toString());
            }
            return resp;
        } catch (Exception e) {
            RespBean resp = new RespBean(0, "", responseString);
            return resp;
        } finally {
            responseBody.close();
        }
    }
}