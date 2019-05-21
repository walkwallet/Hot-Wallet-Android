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
        try {
            ErrorBean error = JSON.parseObject(responseString, ErrorBean.class);
            RespBean resp = new RespBean();
            if (error != null) {
                resp.setCode(error.getError());
                resp.setMsg(error.getMessage());
                resp.setData(responseString);
            } else {
                resp.setCode(0);
                resp.setData(responseString);
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