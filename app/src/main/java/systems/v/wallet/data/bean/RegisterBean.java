package systems.v.wallet.data.bean;

public class RegisterBean {
    private String id;
    private String contractId;
    private ContractContentBean contract;

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public ContractContentBean getContract() {
        return contract;
    }

    public void setContract(ContractContentBean contract) {
        this.contract = contract;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
