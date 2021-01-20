package systems.v.wallet.data.bean;

public class ContractBean {
    private String contractId;
    private ContractInfoBean[] info;
    private String type;

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getType() {
        return type;
    }

    public void setType(String contractType) {
        this.type = contractType;
    }

    public ContractInfoBean[] getInfo() {
        return info;
    }

    public void setInfo(ContractInfoBean[] info) {
        this.info = info;
    }
}
