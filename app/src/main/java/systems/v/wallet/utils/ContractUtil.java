package systems.v.wallet.utils;


import java.util.Locale;

import systems.v.wallet.basic.utils.Base58;
import systems.v.wallet.basic.wallet.ContractFunc;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.data.RetrofitHelper;
import systems.v.wallet.data.api.NodeAPI;
import systems.v.wallet.data.bean.RecordBean;
import vsys.Contract;
import vsys.Vsys;

public class ContractUtil {

    public final static String SplitContractText = "support split";
    public final static String NotSplitContractText = "not support split";

    public static short getFuncIdxByFuncName(ContractFunc[] funcs, String funcName){
        for (short i=0 ;i < funcs.length; i++){
            if(funcs[i].getName().equals(funcName)){
                return i;
            }
        }
        return -1;
    }

    public static String getFunctionTextual(String actionCode, Object... args){
        switch (actionCode){
            case Vsys.ActionInit:
                return String.format(Locale.US,  "init(max=%s, unity=%d, tokenDescription='%s’)", args[0], args[1], args[2]);
            case Vsys.ActionSend:
                return String.format(Locale.US,  "send(recipient='%s', amount=%d)", args[0], args[1]);
            case Vsys.ActionIssue:
            case Vsys.ActionDestroy:
                return String.format(Locale.US, "%s(amount=%d)", actionCode, args[0]);
        }
        return "";
    }

    public static String getFunctionExplain(String actionCode, Object... args){
        switch (actionCode){
            case Vsys.ActionInit:
                return String.format(Locale.US,  "Create token (%s) that max supply is %s", args[0], args[1]);
            case Vsys.ActionSend:
                return String.format(Locale.US, "Send %s token to %s", args[0], args[1]);
            case Vsys.ActionDeposit:
                return String.format(Locale.US, "Deposit %s token to %s", args[0], args[1]);
            case Vsys.ActionWithdraw:
                return String.format(Locale.US, "Withdraw %s token from %s", args[0], args[1]);
            case Vsys.ActionIssue:
            case Vsys.ActionDestroy:
                return String.format(Locale.US, "%s %s token", actionCode, args[0]);
        }
        return "";
    }

    public static Contract generateContract(long unity, long max ,String description, boolean isSplit){
        Contract c = new Contract();
        c.setUnity(unity);
        c.setMax(max);
        c.setTokenDescription(description);
        c.setContract(isSplit ? Base58.decode(Vsys.ConstContractSplit): Base58.decode(Vsys.ConstContractDefault));
        return c;
    }

    public static Contract generateTokenContract(Token token){
        Contract c = new Contract();
        c.setUnity(token.getUnity());
        c.setMax(token.getMax());
        c.setTokenDescription(token.getDescription());
        if (token.isNft()){
            c.setType(Vsys.ContractTypeNft);
            c.setContract(Base58.decode(Vsys.ConstContractNonFungibleToken));
        }else if (token.isSpilt()) {
            c.setType(Vsys.ContractTypeSplit);
            c.setContract(Base58.decode(Vsys.ConstContractSplit));
        }else {
            c.setType(Vsys.ContractTypeDefault);
            c.setContract(Base58.decode(Vsys.ConstContractDefault));
        }
        return c;
    }

    public static Contract generateVsysContract(){
        Contract c = new Contract();
        c.setType(Vsys.ContractTypeSystem);
        c.setUnity(Vsys.VSYS);
        NodeAPI nodeApi = RetrofitHelper.getInstance().getNodeAPI();
        if (nodeApi.isTestNet()) {
            c.setContractId(Constants.TEST_NET_VSYS_CONTRACT_ID);
        }else{
            c.setContractId(Constants.MAIN_NET_VSYS_CONTRACT_ID);
        }
        return c;
    }

    public static Token generateVsysToken() {
        Token token = new Token();
        token.setUnity(Vsys.VSYS);
        token.setName("VSYS");
        token.setVerified(true);
        token.setVsys(true);
        return token;
    }

    public static RecordBean decodeRecordData(Contract c, RecordBean recordBean){
        if (c.getType().equals(Vsys.ContractTypeSystem)) {
            switch (recordBean.getFunctionIndex()) {
                case 1:
                    c.decodeDeposit(Base58.decode(recordBean.getFunctionData()));
                    recordBean.setAmount(c.getAmount());
                    recordBean.setContractId(c.getContractId());
                    break;
                case 2:
                    c.decodeWithdraw(Base58.decode(recordBean.getFunctionData()));
                    recordBean.setAmount(c.getAmount());
                    recordBean.setContractId(c.getContractId());
                    break;
            }
        }else if (c.getType().equals(Vsys.ContractTypeNft)) {
            switch (recordBean.getFunctionIndex()){
                case 2:
                    c.decodeSend(Base58.decode(recordBean.getFunctionData()));
                    recordBean.setAmount(1l);
                    recordBean.setRecipient(c.getRecipient());
                    break;
                case 4:
                    c.decodeDeposit(Base58.decode(recordBean.getFunctionData()));
                    recordBean.setAmount(c.getAmount());
                    recordBean.setContractId(c.getContractId());
                    break;
                case 5:
                    c.decodeWithdraw(Base58.decode(recordBean.getFunctionData()));
                    recordBean.setAmount(c.getAmount());
                    recordBean.setContractId(c.getContractId());
                    break;
            }
        }else if (c.getType().equals(Vsys.ContractTypeSplit)) {
            switch (recordBean.getFunctionIndex()){
                case 4:
                    c.decodeSend(Base58.decode(recordBean.getFunctionData()));
                    recordBean.setAmount(c.getAmount());
                    recordBean.setRecipient(c.getRecipient());
                    break;
                case 6:
                    c.decodeDeposit(Base58.decode(recordBean.getFunctionData()));
                    recordBean.setAmount(c.getAmount());
                    recordBean.setContractId(c.getContractId());
                    break;
                case 7:
                    c.decodeWithdraw(Base58.decode(recordBean.getFunctionData()));
                    recordBean.setAmount(c.getAmount());
                    recordBean.setContractId(c.getContractId());
                    break;
            }
        }else{
            switch (recordBean.getFunctionIndex()){
                case 3:
                    c.decodeSend(Base58.decode(recordBean.getFunctionData()));
                    recordBean.setAmount(c.getAmount());
                    recordBean.setRecipient(c.getRecipient());
                    break;
                case 5:
                    c.decodeDeposit(Base58.decode(recordBean.getFunctionData()));
                    recordBean.setAmount(c.getAmount());
                    recordBean.setContractId(c.getContractId());
                    break;
                case 6:
                    c.decodeWithdraw(Base58.decode(recordBean.getFunctionData()));
                    recordBean.setAmount(c.getAmount());
                    recordBean.setRecipient(c.getRecipient());
                    recordBean.setContractId(c.getContractId());
                    break;
            }
        }

        return recordBean;
    }

    public static boolean isVsysToken(String tokenId) {
        NodeAPI nodeApi = RetrofitHelper.getInstance().getNodeAPI();
        if (nodeApi.isTestNet()) {
            return tokenId.equals(Constants.TEST_NET_VSYS_TOKEN_ID);
        }
        return tokenId.equals(Constants.MAIN_NET_VSYS_TOKEN_ID);
    }

    public static boolean isVsysContract(String contractId) {
        NodeAPI nodeApi = RetrofitHelper.getInstance().getNodeAPI();
        if (nodeApi.isTestNet()) {
            return contractId.equals(Constants.TEST_NET_VSYS_CONTRACT_ID);
        }
        return contractId.equals(Constants.MAIN_NET_VSYS_CONTRACT_ID);
    }
}
