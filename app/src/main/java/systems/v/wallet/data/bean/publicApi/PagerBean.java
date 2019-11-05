package systems.v.wallet.data.bean.publicApi;

public class PagerBean {
    private int Current;
    private int Size;
    private int Total;


    public int getCurrent() {
        return Current;
    }

    public void setCurrent(int current) {
        Current = current;
    }

    public int getSize() {
        return Size;
    }

    public void setSize(int size) {
        Size = size;
    }

    public int getTotal() {
        return Total;
    }

    public void setTotal(int total) {
        Total = total;
    }
}
