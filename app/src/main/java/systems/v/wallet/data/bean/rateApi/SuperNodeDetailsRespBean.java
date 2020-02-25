package systems.v.wallet.data.bean.rateApi;

import java.util.List;

public class SuperNodeDetailsRespBean {
    private int code;
    private String msg;
    private List<SuperNodeDetailsBean> data;

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

    public List<SuperNodeDetailsBean> getData() {
        return data;
    }

    public void setData(List<SuperNodeDetailsBean> data) {
        this.data = data;
    }
}
