package systems.v.wallet.data.bean.publicApi;

public class TokenInfoBean {
    private String Id;
    private String IconUrl;
    private String Name;
    private String Symbol;
    private String Describe;
    private long TotalSupply;
    private String CreatorAddress;
    private int    Index;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
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

    public int getIndex() {
        return Index;
    }

    public void setIndex(int index) {
        Index = index;
    }
}
