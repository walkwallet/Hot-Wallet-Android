package systems.v.wallet.entity;

import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.data.bean.RecordBean;
import systems.v.wallet.utils.DateUtils;

public class RecordEntity extends RecordBean {

    public static final int TYPE_SENT = 1;
    public static final int TYPE_RECEIVED = 2;
    public static final int TYPE_START_OUT_LEASING = 3;
    public static final int TYPE_CANCELED_OUT_LEASING = 4;
    public static final int TYPE_START_IN_LEASING = 5;
    public static final int TYPE_CANCELED_IN_LEASING = 6;
    public static final int TYPE_MINTING = 7;
    public static final int TYPE_REGISTER_CONTRACT = 8;
    public static final int TYPE_EXECUTE_CONTRACT = 9;
    public static final int TYPE_NONE = -1;

    private String address;
    private int recordType;

    public RecordEntity() {
    }

    public RecordEntity(RecordBean bean) {
        setType(bean.getType());
        setId(bean.getId());
        setFee(bean.getFee());
        setTimestamp(bean.getTimestamp());
        setProofs(bean.getProofs());
        setLease(bean.getLease());
        setRecipient(bean.getRecipient());
        setAmount(bean.getAmount());
        setAttachment(bean.getAttachment());
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        int type = getType();
        if (type == Transaction.PAYMENT) {
            if (address.equals(getRecipient())) {
                recordType = TYPE_RECEIVED;
            } else {
                recordType = TYPE_SENT;
            }
        } else if (type == Transaction.LEASE) {
            if (address.equals(getRecipient())) {
                recordType = TYPE_START_IN_LEASING;
            } else {
                recordType = TYPE_START_OUT_LEASING;
            }
        } else if (type == Transaction.CANCEL_LEASE) {
            if (address.equals(getLease().getRecipient())) {
                recordType = TYPE_CANCELED_IN_LEASING;
            } else {
                recordType = TYPE_CANCELED_OUT_LEASING;
            }
        } else if (type == Transaction.MINTING) {
            recordType = TYPE_MINTING;
        } else if (type == Transaction.ContractRegister){
            recordType = TYPE_REGISTER_CONTRACT;
        } else if (type == Transaction.ContractExecute){
            recordType = TYPE_EXECUTE_CONTRACT;
        } else{
            recordType = TYPE_NONE;
        }
        this.address = address;
    }

    public void setRecordType(int recordType) {
        this.recordType = recordType;
    }

    public int getRecordType() {
        return recordType;
    }

    @Override
    public String getRecipient() {
        if (getType() == Transaction.CANCEL_LEASE) {
            return getLease().getRecipient();
        }
        return super.getRecipient();
    }

    @Override
    public long getAmount() {
        if (getType() == Transaction.CANCEL_LEASE) {
            return getLease().getAmount();
        }
        return super.getAmount();
    }

    public long getFormatTimestamp() {
        return DateUtils.getFormat(getTimestamp());
    }
}
