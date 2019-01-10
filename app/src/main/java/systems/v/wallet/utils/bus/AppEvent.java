
package systems.v.wallet.utils.bus;

public class AppEvent {

    AppEventType type;
    String datas;

    public AppEvent() {
    }

    public AppEvent(AppEventType type) {
        this(type, "");
    }

    public AppEvent(AppEventType type, String datas) {
        this.type = type;
        this.datas = datas;
    }

    public AppEventType getType() {
        return type;
    }

    public void setType(AppEventType type) {
        this.type = type;
    }

    public String getDatas() {
        return datas;
    }

    public void setDatas(String datas) {
        this.datas = datas;
    }
}