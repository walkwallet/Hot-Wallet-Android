package systems.v.wallet.entity;

import java.util.List;

import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.data.bean.RecordBean;
import systems.v.wallet.utils.ContractUtil;
import systems.v.wallet.utils.DateUtils;
import vsys.Vsys;

public class RecordEntity extends RecordBean implements Cloneable{

    public static final int TYPE_SENT = 1;
    public static final int TYPE_RECEIVED = 2;
    public static final int TYPE_START_OUT_LEASING = 3;
    public static final int TYPE_CANCELED_OUT_LEASING = 4;
    public static final int TYPE_START_IN_LEASING = 5;
    public static final int TYPE_CANCELED_IN_LEASING = 6;
    public static final int TYPE_MINTING = 7;
    public static final int TYPE_REGISTER_CONTRACT = 8;
    public static final int TYPE_EXECUTE_CONTRACT = 9;
    public static final int TYPE_EXECUTE_CONTRACT_SENT = 91;
    public static final int TYPE_EXECUTE_CONTRACT_RECEIVED = 92;
    public static final int TYPE_EXECUTE_CONTRACT_WITHDRAW = 93;
    public static final int TYPE_EXECUTE_CONTRACT_DEPOSIT = 94;

    public static final int TYPE_NONE = -1;

    private String address;
    private int recordType;
    private Token token;
    private String sender;

    public static final String SUCCESS_TX = "Success";

    public RecordEntity() {
    }

    public RecordEntity(RecordBean bean, List<Token> verifiedTokens, String address) {
        setType(bean.getType());
        setId(bean.getId());
        setFee(bean.getFee());
        setTimestamp(bean.getTimestamp());
        setProofs(bean.getProofs());
        setLease(bean.getLease());
        // decode token transaction
        if (bean.getType() == TYPE_EXECUTE_CONTRACT && verifiedTokens != null) {
            for (Token token : verifiedTokens) {
                if (Vsys.tokenId2ContractId(token.getTokenId()).equals(bean.getContractId())) {
                    bean = ContractUtil.decodeRecordData(ContractUtil.generateContract(token.getUnity(), token.getMax(), token.getDescription(), token.isSpilt()), bean);
                    setToken(token);
                    break;
                }
            }
        }
        setFunctionIndex(bean.getFunctionIndex());
        setRecipient(bean.getRecipient());
        setAmount(bean.getAmount());
        setAttachment(bean.getAttachment());
        setStatus(bean.getStatus());
        if (bean.getProofs() != null && bean.getProofs().size() != 0 ){
            setSender(bean.getProofs().get(0).getAddress());
        } else{
            setSender("");
        }
        setAddress(address);
        initRecordType();
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void initRecordType() {
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
        } else if (type == Transaction.CONTRACT_REGISTER){
            recordType = TYPE_REGISTER_CONTRACT;
        } else if (type == Transaction.CONTRACT_EXECUTE){
            recordType = TYPE_EXECUTE_CONTRACT;
            if(getAmount() != 0 && getToken() != null) {
                if (getToken().isSpilt()) {
                    switch (getFunctionIndex()) {
                        case 4:
                            if (getAddress().equals(getRecipient())) {
                                recordType = TYPE_EXECUTE_CONTRACT_RECEIVED;
                            } else {
                                recordType = TYPE_EXECUTE_CONTRACT_SENT;
                            }
                            break;
                        case 6:
                            recordType = TYPE_EXECUTE_CONTRACT_DEPOSIT;
                            break;
                        case 7:
                            recordType = TYPE_EXECUTE_CONTRACT_WITHDRAW;
                            break;
                    }
                }else{
                    switch (getFunctionIndex()) {
                        case 3:
                            if (getAddress().equals(getRecipient())) {
                                recordType = TYPE_EXECUTE_CONTRACT_RECEIVED;
                            } else {
                                recordType = TYPE_EXECUTE_CONTRACT_SENT;
                            }
                            break;
                        case 5:
                            recordType = TYPE_EXECUTE_CONTRACT_DEPOSIT;
                            break;
                        case 6:
                            recordType = TYPE_EXECUTE_CONTRACT_WITHDRAW;
                            break;
                    }
                }
            }
        } else{
            recordType = TYPE_NONE;
        }
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

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
