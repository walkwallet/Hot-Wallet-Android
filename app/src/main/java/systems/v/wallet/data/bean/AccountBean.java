package systems.v.wallet.data.bean;

import systems.v.wallet.basic.wallet.AccountBalance;

public class AccountBean implements AccountBalance {
    /**
     * Total = Available + Lease out = Regular
     * Effective = Available + Lease in
     */
    private String address;
    private long regular;//total
    private long mintingAverage;
    private long available;
    private long effective;
    private int height;

    public AccountBean() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getRegular() {
        return regular;
    }

    public void setRegular(long regular) {
        this.regular = regular;
    }

    public long getMintingAverage() {
        return mintingAverage;
    }

    public void setMintingAverage(long mintingAverage) {
        this.mintingAverage = mintingAverage;
    }

    public long getAvailable() {
        return available;
    }

    public void setAvailable(long available) {
        this.available = available;
    }

    public long getEffective() {
        return effective;
    }

    public void setEffective(long effective) {
        this.effective = effective;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setAccountBean(AccountBean accountBean) {
        this.address = accountBean.address;
        this.regular = accountBean.regular;
        this.available = accountBean.available;
        this.mintingAverage = accountBean.mintingAverage;
        this.effective = accountBean.effective;
        this.height = accountBean.height;
    }
}
