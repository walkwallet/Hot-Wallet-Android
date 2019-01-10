package systems.v.wallet.utils;

import java.util.List;

import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.data.bean.LeaseBean;
import systems.v.wallet.entity.RecordEntity;

public class DataUtil {

    public static boolean isCancelable(List<RecordEntity> list, RecordEntity record) {
        String txId = record.getId();
        if (record.getType() != Transaction.LEASE) {
            return false;
        }
        for (RecordEntity entity : list) {
            if (entity.getType() == Transaction.CANCEL_LEASE) {
                LeaseBean leaseBean = entity.getLease();
                if (leaseBean != null && leaseBean.getId().equals(txId)) {
                    return false;
                }
            }
        }
        return true;
    }
}
