package systems.v.wallet.data.bean;

import java.util.List;

public class RecordRespBean {
    private int totalCount;
    private int size;
    private List<RecordBean> transactions;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<RecordBean> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<RecordBean> transactions) {
        this.transactions = transactions;
    }
}
