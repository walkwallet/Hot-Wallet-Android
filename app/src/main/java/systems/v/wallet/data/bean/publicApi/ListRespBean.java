package systems.v.wallet.data.bean.publicApi;

public class ListRespBean {
    private Object[] list;
    private PagerBean pager;

    public Object[] getList() {
        return list;
    }

    public void setList(Object[] list) {
        this.list = list;
    }

    public PagerBean getPager() {
        return pager;
    }

    public void setPager(PagerBean pager) {
        this.pager = pager;
    }
}
