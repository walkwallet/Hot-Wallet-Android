package systems.v.wallet.data.bean.publicApi;

public class TokenInfoBean {
    private String id;
    private String IconUrl;
    private String Name;
    private String Symbol;
    private String Describe;
    private long TotalSupply;
    private String CreatorAddress;
    private String TokenId;
    private int    Index;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIconUrl() {
        return IconUrl;
    }

    public void setIconUrl(String iconUrl) {
        IconUrl = iconUrl;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getSymbol() {
        return Symbol;
    }

    public void setSymbol(String symbol) {
        Symbol = symbol;
    }

    public String getDescribe() {
        return Describe;
    }

    public void setDescribe(String describe) {
        Describe = describe;
    }

    public long getTotalSupply() {
        return TotalSupply;
    }

    public void setTotalSupply(long totalSupply) {
        TotalSupply = totalSupply;
    }

    public String getCreatorAddress() {
        return CreatorAddress;
    }

    public void setCreatorAddress(String creatorAddress) {
        CreatorAddress = creatorAddress;
    }

    public String getTokenId() {
        return TokenId;
    }

    public void setTokenId(String tokenId) {
        TokenId = tokenId;
    }

    public int getIndex() {
        return Index;
    }

    public void setIndex(int index) {
        Index = index;
    }
}
