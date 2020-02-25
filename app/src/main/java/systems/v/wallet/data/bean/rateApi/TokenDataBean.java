package systems.v.wallet.data.bean.rateApi;

public class TokenDataBean {
    String name;
    String logo;
    long mint_amount;
    int mint_day;
    String return_rate;
    String TokenCycle;
    String TokenFee;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public long getMint_amount() {
        return mint_amount;
    }

    public void setMint_amount(long mint_amount) {
        this.mint_amount = mint_amount;
    }

    public int getMint_day() {
        return mint_day;
    }

    public void setMint_day(int mint_day) {
        this.mint_day = mint_day;
    }

    public String getReturn_rate() {
        return return_rate;
    }

    public void setReturn_rate(String return_rate) {
        this.return_rate = return_rate;
    }

    public String getTokenCycle() {
        return TokenCycle;
    }

    public void setTokenCycle(String tokenCycle) {
        TokenCycle = tokenCycle;
    }

    public String getTokenFee() {
        return TokenFee;
    }

    public void setTokenFee(String tokenFee) {
        TokenFee = tokenFee;
    }
}
