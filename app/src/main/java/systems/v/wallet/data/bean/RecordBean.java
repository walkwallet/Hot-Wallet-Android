package systems.v.wallet.data.bean;

import java.util.List;

public class RecordBean {

    private int type;
    private String id;
    private long fee;
    private long timestamp;
    private List<ProofBean> proofs;
    private LeaseBean lease;
    private String recipient;
    private long amount;
    private String attachment;
    private String status;

    public RecordBean() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public long getTimestamp() {
        if (String.valueOf(timestamp).length() == 19) {
            return timestamp / 1000000;
        }
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<ProofBean> getProofs() {
        return proofs;
    }

    public void setProofs(List<ProofBean> proofs) {
        this.proofs = proofs;
    }

    public LeaseBean getLease() {
        return lease;
    }

    public void setLease(LeaseBean leaseBean) {
        this.lease = leaseBean;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
