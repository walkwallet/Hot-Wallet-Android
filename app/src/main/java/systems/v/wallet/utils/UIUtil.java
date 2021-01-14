package systems.v.wallet.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.TxUtil;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.basic.wallet.Token;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.databinding.ItemInfoHorizontalBinding;
import systems.v.wallet.databinding.ItemInfoVerticalBinding;
import systems.v.wallet.databinding.ItemInfoVerticalCopyableBinding;
import vsys.Vsys;

public class UIUtil {

    public static ItemInfoHorizontalBinding addItem(LayoutInflater inflater, ViewGroup container, @StringRes int resId, String text) {
        ItemInfoHorizontalBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_info_horizontal, null, false);
        binding.tvTitle.setText(resId);
        binding.tvText.setText(text);
        container.addView(binding.getRoot());
        return binding;
    }

    public static ItemInfoVerticalBinding addItemVertical(LayoutInflater inflater, ViewGroup container, @StringRes int resId, String text) {
        ItemInfoVerticalBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_info_vertical, null, false);
        binding.tvTitle.setText(resId);
        binding.tvText.setText(text);
        container.addView(binding.getRoot());
        return binding;
    }

    public static ItemInfoVerticalCopyableBinding addItemVerticalCopyable(final Activity activity, LayoutInflater inflater, ViewGroup container, @StringRes int resId, final String text) {
        ItemInfoVerticalCopyableBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_info_vertical_copyable, null, false);
        binding.tvTitle.setText(resId);
        binding.tvText.setText(text);
        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtil.copyToClipboard(activity, text);
            }
        });
        container.addView(binding.getRoot());
        return binding;
    }

    public static void addTransactionDetail(LayoutInflater inflater, final ViewGroup container, final Transaction tx) {
        int type = tx.getTransactionType();
        final Account sender = App.getInstance().getWallet().getAccount(tx.getSenderPublicKey());

        if (type == Transaction.PAYMENT){
            ItemInfoVerticalBinding bindingFrom = addItemVertical(inflater, container, R.string.send_review_my_address, sender.getAddress());
            bindingFrom.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIUtil.copyToClipboard(container.getContext(), sender.getAddress());
                }
            });
            ItemInfoVerticalBinding bindingTo = addItemVertical(inflater, container, R.string.send_review_send_to, tx.getRecipient());
            bindingTo.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIUtil.copyToClipboard(container.getContext(), tx.getRecipient());
                }
            });

            addItemVertical(inflater, container, R.string.send_review_type, TxUtil.getTypeText(inflater.getContext(), tx.getTransactionType()));
            addItemVertical(inflater, container, R.string.send_amount, CoinUtil.formatWithUnit(tx.getAmount()));
            addItemVertical(inflater, container, R.string.send_fee, CoinUtil.formatWithUnit(tx.getFee()));
            addItemVertical(inflater, container, R.string.send_description, tx.getAttachment());
        }else if(type == Transaction.LEASE){
            ItemInfoVerticalBinding bindingFrom = addItemVertical(inflater, container, R.string.send_review_my_address, sender.getAddress());
            bindingFrom.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIUtil.copyToClipboard(container.getContext(), sender.getAddress());
                }
            });
            ItemInfoVerticalBinding bindingTo = addItemVertical(inflater, container, R.string.send_review_lease_to, tx.getRecipient());
            bindingTo.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIUtil.copyToClipboard(container.getContext(), tx.getRecipient());
                }
            });
            addItemVertical(inflater, container, R.string.send_review_type, TxUtil.getTypeText(inflater.getContext(), tx.getTransactionType()));
            addItemVertical(inflater, container, R.string.send_amount, CoinUtil.formatWithUnit(tx.getAmount()));
            addItemVertical(inflater, container, R.string.send_fee, CoinUtil.formatWithUnit(tx.getFee()));
        }else if(type == Transaction.CANCEL_LEASE){
            ItemInfoVerticalBinding bindingFrom = addItemVertical(inflater, container, R.string.send_review_lease_to, sender.getAddress());
            bindingFrom.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIUtil.copyToClipboard(container.getContext(), sender.getAddress());
                }
            });
            addItemVertical(inflater, container, R.string.send_review_type, TxUtil.getTypeText(inflater.getContext(), tx.getTransactionType()));
            addItemVertical(inflater, container, R.string.send_fee, CoinUtil.formatWithUnit(tx.getFee()));

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            String time = dateFormat.format(new Timestamp(tx.getTimestamp()));
            addItemVertical(inflater, container, R.string.send_time, String.format("%s (%s)", time, TimeZone.getDefault().getDisplayName()));
        }else if(type == Transaction.CONTRACT_REGISTER){
            addItemVertical(inflater, container, R.string.create_review_token_total_tokens, CoinUtil.format(tx.getContractObj().getMax(), tx.getContractObj().getUnity()));
            addItemVertical(inflater, container, R.string.send_review_type, TxUtil.getTypeText(inflater.getContext(), tx.getTransactionType()));
            ItemInfoVerticalBinding bindingFrom = addItemVertical(inflater, container, R.string.send_review_from, sender.getAddress());
            bindingFrom.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIUtil.copyToClipboard(container.getContext(), sender.getAddress());
                }
            });
            addItemVertical(inflater, container, R.string.send_description, tx.getContractObj().getTokenDescription());
            addItemVertical(inflater, container, R.string.send_fee, CoinUtil.formatWithUnit(tx.getFee()));
        }else if(type == Transaction.CONTRACT_EXECUTE){
            String action = tx.getActionCode();

            if (action.equals(Vsys.ActionIssue)){
                addItemVertical(inflater, container, R.string.issue_token_review_amount, CoinUtil.format(tx.getContractObj().getAmount(), tx.getContractObj().getUnity()));
                addItemVertical(inflater, container, R.string.send_review_type, action);
                ItemInfoVerticalBinding bindingFrom = addItemVertical(inflater, container, R.string.send_review_from, sender.getAddress());
                bindingFrom.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIUtil.copyToClipboard(container.getContext(), sender.getAddress());
                    }
                });
                addItemVertical(inflater, container, R.string.send_fee, CoinUtil.formatWithUnit(tx.getFee()));
            }else if(action.equals(Vsys.ActionDestroy)){
                addItemVertical(inflater, container, R.string.destroy_token_review_amount, CoinUtil.format(tx.getContractObj().getAmount(), tx.getContractObj().getUnity()));
                addItemVertical(inflater, container, R.string.send_review_type, action);
                ItemInfoVerticalBinding bindingFrom = addItemVertical(inflater, container, R.string.send_review_from, sender.getAddress());
                bindingFrom.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIUtil.copyToClipboard(container.getContext(), sender.getAddress());
                    }
                });
                addItemVertical(inflater, container, R.string.send_fee, CoinUtil.formatWithUnit(tx.getFee()));
            }else if(action.equals(Vsys.ActionSend)){
                ItemInfoVerticalBinding bindingFrom = addItemVertical(inflater, container, R.string.send_review_from, sender.getAddress());
                bindingFrom.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIUtil.copyToClipboard(container.getContext(), sender.getAddress());
                    }
                });
                ItemInfoVerticalBinding bindingTo = addItemVertical(inflater, container, R.string.send_review_send_to, tx.getContractObj().getRecipient());
                bindingTo.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIUtil.copyToClipboard(container.getContext(), tx.getContractObj().getRecipient());
                    }
                });
                addItemVertical(inflater, container, R.string.send_review_type, action);
                addItemVertical(inflater, container, R.string.send_token_review_amount, CoinUtil.format(tx.getContractObj().getAmount(), tx.getContractObj().getUnity()));
                addItemVertical(inflater, container, R.string.send_fee, CoinUtil.formatWithUnit(tx.getFee()));
                addItemVertical(inflater, container, R.string.send_description, tx.getAttachment());
            }
        }
    }

    public static void addTokenDetail(LayoutInflater inflater, final ViewGroup container, final Token token){
        ItemInfoVerticalBinding bindingTokeId = addItemVertical(inflater, container, R.string.token_info_token_id, token.getTokenId());
        bindingTokeId.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtil.copyToClipboard(container.getContext(), token.getTokenId());
            }
        });
        addItemVertical(inflater, container, R.string.token_info_contract_id, Vsys.tokenId2ContractId(token.getTokenId()));
        addItemVertical(inflater, container, R.string.token_info_issuer, token.getIssuer());
        addItemVertical(inflater, container, R.string.token_info_maker, token.getMaker());
        addItemVertical(inflater, container, R.string.token_info_total_token, CoinUtil.format(token.getMax(), token.getUnity()));
        addItemVertical(inflater, container, R.string.token_info_unity, Long.toString(token.getUnity()));
        addItemVertical(inflater, container, R.string.token_info_issued_tokens, CoinUtil.format(token.getIssuedAmount(), token.getUnity()));
        addItemVertical(inflater, container, R.string.token_info_description, token.getDescription());
    }

    public static void showUnsupportQrCodeDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage(R.string.unsupported_qrcode)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }

    public static void showInconsistentNetworkTestnetDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage(R.string.inconsistent_network_testnet)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }

    public static void showInconsistentNetworkMainnetDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage(R.string.inconsistent_network_mainnet)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }

    public static AlertDialog showInfo(Context context, @StringRes int resId) {
        return new AlertDialog.Builder(context)
                .setMessage(resId)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void saveBitmap2Gallery(Context context, Bitmap bmp, String picName) {

        String fileName = null;
        String galleryPath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera" + File.separator;

        File file = null;
        FileOutputStream outStream = null;

        try {
            file = new File(galleryPath, picName + ".jpg");

            fileName = file.toString();
            outStream = new FileOutputStream(fileName);
            if (null != outStream) {
                bmp.compress(Bitmap.CompressFormat.PNG, 90, outStream);
            }

        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // send broadcast
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    bmp, fileName, null);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            context.sendBroadcast(intent);
            ToastUtil.showToast(R.string.receive_save_image_success);

        }
    }

    public static void setAmountInputFilter(EditText editText) {
        editText.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                StringBuilder builder = new StringBuilder(dest);
                if (dstart != dend) {
                    builder.delete(dstart, dend);
                }
                if (start != end) {
                    builder.insert(dstart, source, start, end);
                }
                String result = builder.toString();
                if (CoinUtil.validate(result)) {
                    return null;
                }
                return "";
            }
        }});
    }

    public static void setAmountInputFilterWithScale(EditText editText, final long unity){
        editText.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                StringBuilder builder = new StringBuilder(dest);
                if (dstart != dend) {
                    builder.delete(dstart, dend);
                }
                if (start != end) {
                    builder.insert(dstart, source, start, end);
                }
                String result = builder.toString();
                if (CoinUtil.validate(result, unity)) {
                    return null;
                }
                return "";
            }
        }});
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("vsys", text);
        assert clipboard != null;
        clipboard.setPrimaryClip(clipData);
        ToastUtil.showToast(R.string.receive_copy_success);
    }

    public static String getMutatedAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return "";
        }
        String start, middle, end;
        int len = address.length();

        if (len > 6) {
            start = address.substring(0, 6);
            middle = "******";
            end = address.substring(len - 6, len);
            return start + middle + end;
        } else {
            return "";
        }
    }
}
