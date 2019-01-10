package systems.v.wallet.data.bean;

import com.alibaba.fastjson.JSON;

/**
 * response
 */
public class RespBean {

    private int code;
    private String msg;
    private String data;

    public RespBean() {
    }

    public RespBean(int code, String msg, String data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
