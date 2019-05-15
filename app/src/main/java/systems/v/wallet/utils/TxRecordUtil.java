package systems.v.wallet.utils;

import android.content.Context;

import systems.v.wallet.R;
import systems.v.wallet.entity.RecordEntity;

public class TxRecordUtil {

    public static String getTypeText(Context context, int type) {
        int textId = 0;
        switch (type) {
            case RecordEntity.TYPE_RECEIVED:
            case RecordEntity.TYPE_MINTING:
                textId = R.string.detail_received;
                break;
            case RecordEntity.TYPE_SENT:
                textId = R.string.detail_sent;
                break;
            case RecordEntity.TYPE_START_IN_LEASING:
                textId = R.string.detail_incoming_leasing;
                break;
            case RecordEntity.TYPE_CANCELED_IN_LEASING:
                textId = R.string.detail_canceled_int_leasing;
                break;
            case RecordEntity.TYPE_START_OUT_LEASING:
                textId = R.string.detail_start_leasing;
                break;
            case RecordEntity.TYPE_CANCELED_OUT_LEASING:
                textId = R.string.detail_canceled_out_leasing;
                break;
            case RecordEntity.TYPE_REGISTER_CONTRACT:
                textId = R.string.detail_create_contract;
                break;
            case RecordEntity.TYPE_EXECUTE_CONTRACT:
                textId = R.string.detail_execute_contract;
                break;
        }
        if (textId != 0) {
            return context.getString(textId);
        }
        return "";
    }
}
