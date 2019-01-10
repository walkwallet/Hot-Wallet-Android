package systems.v.wallet.entity;

import systems.v.wallet.basic.wallet.Account;

public class AccountEntity extends Account {

    private boolean headerWallet = false;
    private boolean headerMonitor = false;

    public AccountEntity() {
    }

    public AccountEntity(Account account) {
        setNonce(account.getNonce());
        setNetwork(account.getNetwork());
        setSeed(account.getSeed());
        setAddress(account.getAddress());
        setPublicKey(account.getPublicKey());
        setAccount(account.getAccount());
        updateBalance(account);
    }


    public boolean isHeaderWallet() {
        return headerWallet;
    }

    public void setHeaderWallet(boolean headerWallet) {
        this.headerWallet = headerWallet;
    }

    public boolean isHeaderMonitor() {
        return headerMonitor;
    }

    public void setHeaderMonitor(boolean headerMonitor) {
        this.headerMonitor = headerMonitor;
    }
}
