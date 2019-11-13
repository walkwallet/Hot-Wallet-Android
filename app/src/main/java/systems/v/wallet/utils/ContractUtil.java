package systems.v.wallet.utils;


import android.util.Log;

import java.util.Locale;

import systems.v.wallet.basic.utils.Base58;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.wallet.ContractFunc;
import systems.v.wallet.basic.wallet.Token;
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

    public static RecordBean decodeRecordData(Contract c, RecordBean recordBean){
        boolean isSplit = Base58.encode(c.getContract()).equals(Vsys.ConstContractSplit);
        if (isSplit){
            switch (recordBean.getFunctionIndex()){
                case 4:
                    c.decodeSend(Base58.decode(recordBean.getFunctionData()));
                    recordBean.setAmount(c.getAmount());
                    recordBean.setRecipient(c.getRecipient());
                    break;
            }
        }else{
            switch (recordBean.getFunctionIndex()){
                case 3:
                    c.decodeSend(Base58.decode(recordBean.getFunctionData()));
                    recordBean.setAmount(c.getAmount());
                    recordBean.setRecipient(c.getRecipient());
                    break;
            }
        }

        return recordBean;
    }
}
