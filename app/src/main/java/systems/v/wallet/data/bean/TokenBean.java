package systems.v.wallet.data.bean;

public class TokenBean {
    private String address;
    private String tokenId;
    private long balance;
    private String contractId;
    private TokenInfoBean[] info;

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



    public TokenInfoBean[] getInfo() {
        return info;
    }

    public void setInfo(TokenInfoBean[] info) {
        this.info = info;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }
}
