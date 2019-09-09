package systems.v.wallet.data.bean;

public class ContractBean {
    private String contractId;
    private ContractInfoBean[] info;

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public ContractInfoBean[] getInfo() {
        return info;
    }

    public void setInfo(ContractInfoBean[] info) {
        this.info = info;
    }
}
