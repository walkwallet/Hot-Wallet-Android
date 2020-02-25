package systems.v.wallet.data.bean.rateApi;

import java.util.List;

public class SuperNodeDetailsBean {
    private String Address;
    private long LeaseInBalance;
    private boolean IsSuperNode;
    private long DailyEfficiency;
    private long MonthlyEfficiency;
    private String logo;
    private String name;
    private String vote_address;
    private String location;
    private double fee;
    private String capacity;
    private String cycle;
    private String url;
    private List<TokenDataBean> TokenData;
    private List<SubNodeBean> SubNode;

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public long getLeaseInBalance() {
        return LeaseInBalance;
    }

    public void setLeaseInBalance(long leaseInBalance) {
        LeaseInBalance = leaseInBalance;
    }

    public boolean isSuperNode() {
        return IsSuperNode;
    }

    public void setIsSuperNode(boolean superNode) {
        IsSuperNode = superNode;
    }

    public long getDailyEfficiency() {
        return DailyEfficiency;
    }

    public void setDailyEfficiency(long dailyEfficiency) {
        DailyEfficiency = dailyEfficiency;
    }

    public long getMonthlyEfficiency() {
        return MonthlyEfficiency;
    }

    public void setMonthlyEfficiency(long monthlyEfficiency) {
        MonthlyEfficiency = monthlyEfficiency;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVote_address() {
        return vote_address;
    }

    public void setVote_address(String vote_address) {
        this.vote_address = vote_address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<TokenDataBean> getTokenData() {
        return TokenData;
    }

    public void setTokenData(List<TokenDataBean> tokenData) {
        TokenData = tokenData;
    }

    public List<SubNodeBean> getSubNode() {
        return SubNode;
    }

    public void setSubNode(List<SubNodeBean> subNode) {
        SubNode = subNode;
    }
}
