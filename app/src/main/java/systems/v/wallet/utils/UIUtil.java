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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import systems.v.wallet.App;
import systems.v.wallet.R;
import systems.v.wallet.basic.utils.CoinUtil;
import systems.v.wallet.basic.utils.TxUtil;
import systems.v.wallet.basic.wallet.Account;
import systems.v.wallet.basic.wallet.Transaction;
import systems.v.wallet.databinding.ItemTransactionInfoBinding;
import systems.v.wallet.databinding.ItemTransactionInfoVerticalBinding;

public class UIUtil {

    public static ItemTransactionInfoBinding addItem(LayoutInflater inflater, ViewGroup container, @StringRes int resId, String text) {
        ItemTransactionInfoBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_transaction_info, null, false);
        binding.tvTitle.setText(resId);
        binding.tvText.setText(text);
        container.addView(binding.getRoot());
        return binding;
    }

    public static ItemTransactionInfoVerticalBinding addItemVertical(LayoutInflater inflater, ViewGroup container, @StringRes int resId, String text) {
        ItemTransactionInfoVerticalBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_transaction_info_vertical, null, false);
        binding.tvTitle.setText(resId);
        binding.tvText.setText(text);
        container.addView(binding.getRoot());
        return binding;
    }

    public static void addTransactionDetail(LayoutInflater inflater, final ViewGroup container, final Transaction tx) {
        int type = tx.getTransactionType();
        final Account sender = App.getInstance().getWallet().getAccount(tx.getSenderPublicKey());

        ItemTransactionInfoVerticalBinding bindingFrom =
                addItemVertical(inflater, container, R.string.send_review_from, sender.getAddress());
        bindingFrom.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtil.copyToClipboard(container.getContext(), sender.getAddress());
            }
        });

        int titleId = type == Transaction.PAYMENT
                ? R.string.send_review_send_to : R.string.send_review_lease_to;
        ItemTransactionInfoVerticalBinding bindingTo =
                addItemVertical(inflater, container, titleId, tx.getRecipient());
        bindingTo.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtil.copyToClipboard(container.getContext(), tx.getRecipient());
            }
        });

        addItemVertical(inflater, container, R.string.send_review_type,
                TxUtil.getTypeText(inflater.getContext(), tx.getTransactionType()));
        if (type == Transaction.PAYMENT || type == Transaction.LEASE) {
            addItemVertical(inflater, container, R.string.send_amount,
                    CoinUtil.formatWithUnit(tx.getAmount()));
        }
        addItemVertical(inflater, container, R.string.send_fee,
                CoinUtil.formatWithUnit(tx.getFee()));

        if (type == Transaction.CANCEL_LEASE) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
            String time = dateFormat.format(new Timestamp(tx.getTimestamp()));
            addItemVertical(inflater, container, R.string.send_time,
                    String.format("%s (%s)", time, TimeZone.getDefault().getDisplayName()));
        }

        if (type == Transaction.PAYMENT && !TextUtils.isEmpty(tx.getAttachment())) {
            addItemVertical(inflater, container, R.string.send_description,
                    TxUtil.decodeAttachment(tx.getAttachment()));
        }
    }

    public static void showUnsupportQrCodeDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage(R.string.unsupported_qrcode)
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
