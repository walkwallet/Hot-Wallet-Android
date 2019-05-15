package systems.v.wallet.data.bean;

public class TokenBean {
    private String tokenId;
    private long max;
    private long total;
    private long unity;
    private String description;

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getUnity() {
        return unity;
    }

    public void setUnity(long unity) {
        this.unity = unity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
