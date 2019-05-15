package systems.v.wallet.data.bean;

public class TokenBalanceBean {
    private String address;
    private String tokenId;
    private long balance;
    private long unity;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getUnity() {
        return unity;
    }

    public void setUnity(long unity) {
        this.unity = unity;
    }
}
