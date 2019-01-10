package systems.v.wallet.data.bean;

import java.util.List;

public class ResultBean {

    private int type;
    private String id;
    private long fee;
    private long timestamp;
    private String recipient;
    private short feeScale;
    private long amount;
    private String attachment;
    private List<ProofBean> proofs;

    public ResultBean() {
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
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public short getFeeScale() {
        return feeScale;
    }

    public void setFeeScale(short feeScale) {
        this.feeScale = feeScale;
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

    public List<ProofBean> getProofs() {
        return proofs;
    }

    public void setProofs(List<ProofBean> proofs) {
        this.proofs = proofs;
    }
}
