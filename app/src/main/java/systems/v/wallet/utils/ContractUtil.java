package systems.v.wallet.utils;


import java.util.Locale;

import systems.v.wallet.basic.utils.Base58;
import systems.v.wallet.basic.wallet.ContractFunc;
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
                return String.format(Locale.US,  "init(max=%d, unity=%d, tokenDescription='%s’)", args[0], args[1], args[2]);
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

    public static void main(String args[]){
        System.out.println(getFunctionTextual(Vsys.ActionInit, 20000000, 23020, "sasdf"));
        System.out.println(getFunctionTextual(Vsys.ActionSend, "AUx1234791759GPWON", 2));
        System.out.println(getFunctionTextual(Vsys.ActionIssue, 20000000));
        System.out.println(getFunctionTextual(Vsys.ActionDestroy, 2));

        System.out.println(getFunctionTextual(Vsys.ActionInit, 20000000, 23020, "sasdf"));
        System.out.println(getFunctionTextual(Vsys.ActionSend, "AUx1234791759GPWON", 2));
        System.out.println(getFunctionTextual(Vsys.ActionIssue, 20000000));
        System.out.println(getFunctionTextual(Vsys.ActionDestroy, 2));
        if(Vsys.ConstContractDefault.equals("3GQnJtxDQc3zFuUwXKbrev1TL7VGxk5XNZ7kUveKK6BsneC1zTSTRjgBTdDrksHtVMv6nwy9Wy 6MHRgydAJgEegDmL4yx7tdNjdnU38b8FrCzFhA1aRNxhEC3ez7JCi3a5dgVPr93hS96XmSDnHYv yiCuL6dggahs2hKXjdz4SGgyiUUP4246xnELkjhuCF4KqRncUDcZyWQA8UrfNCNSt9MRKTj89sKs V1hbcGaTcX2qqqSU841HyokLcoQSgmaP3uBBMdgSYVtovPLEFmpXFMoHWXAxQZDaEtZcHPkr hJyG6CdTgkNLUQKWtQdYzjxCc9AsUGMJvWrxWMi6RQpcqYk3aszbEyAh4r4fcszHHAJg64ovDg MNUDnWQWJerm5CjvN76J2MVN6FqQkS9YrM3FoHFTj1weiRbtuTc3mCR4iMcu2eoxcGYRmUHx KiRoZcWnWMX2mzDw31SbvHqqRbF3t44kouJznTyJM6z1ruiyQW6LfFZuV6VxsKLX3KQ46SxNsa JoUpvaXmVj2hULoGKHpwPrTVzVpzKvYQJmz19vXeZiqQ2J3tVcSFH17ahSzwRkXYJ5HP655FHq Tr6Vvt8pBt8N5vixJdYtfx7igfKX4aViHgWkreAqBK3trH4VGJ36e28RJP8Xrt6NYG2icsHsoERqHik7G djPAmXpnffDL6P7NBfyKWtp9g9C289TDGUykS8CNiW9L4sbUabdrqsdkdPRjJHzzrb2gKTf2vB56r ZmreTUbJ53KsvpZht5bixZ59VbCNZaHfZyprvzzhyTAudAmhp8Nrks7SV1wTySZdmfLyw7vsNmTEi 3hmuPmYqExp4PoLPUwT4TYt2doYUX1ds3CesnRSjFqMhXnLmTgYXsAXvvT2E6PWTY5nPCycQ v5pozvQuw1onFtGwY9n5s2VFjxS9W6FkCiqyyZAhCXP5o44wkmD5SVqyqoL5HmgNc8SJL7uMM MDDwecy7Sh9vvt3RXirH7F7bpUv3VsaepVGCHLfDp9GMG59ZiWK9Rmzf66e8Tw4unphu7gFNZu qeBk2YjCBj3i4eXbJvBEgCRB51FATRQY9JUzdMv9Mbkaq4DW69AgdqbES8aHeoax1UDDBi3raM 8WpP2cKVEqoeeCGYM2vfN6zBAh7Tu3M4NcNFJmkNtd8Mpc2Md1kxRsusVzHiYxnsZjo".replace(" ","")))
            System.out.println(Base58.encode("gdgdg".getBytes()));

    }
}
