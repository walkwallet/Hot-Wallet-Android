package systems.v.wallet.utils;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.core.os.CancellationSignal;

public class FingerprintManagerUtil {
    private static FingerprintManagerCompat fingerprintManagerCompat;
    private static CancellationSignal cancellationSignal;

    private FingerprintManagerUtil() {
    }

    public static boolean isAvailable(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager =
                    (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            // fingerprint hardware exist and available
            return fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints();
        } else {
            return false;
        }
    }

    /**
     * start verify fingerprint
     *
     * @param context
     * @param fingerprintListener callback
     */
    public static void startFingerprinterVerification(Context context, final FingerprintListener fingerprintListener) {
        fingerprintManagerCompat = FingerprintManagerCompat.from(context);

        if (fingerprintManagerCompat == null || !fingerprintManagerCompat.isHardwareDetected()) {
            if (fingerprintListener != null)
                fingerprintListener.onNonsupport();
            return;
        }

        if (!fingerprintManagerCompat.hasEnrolledFingerprints()) {
            if (fingerprintListener != null)
                fingerprintListener.onEnrollFailed();
            return;
        }

        if (fingerprintListener != null)
            fingerprintListener.onAuthenticationStart();

        cancellationSignal = new CancellationSignal();
        fingerprintManagerCompat.authenticate(null, 0, cancellationSignal, new FingerprintManagerCompat.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationError(errMsgId, errString);
                if (fingerprintListener != null)
                    fingerprintListener.onAuthenticationError(errMsgId, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
                if (fingerprintListener != null)
                    fingerprintListener.onAuthenticationHelp(helpMsgId, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (fingerprintListener != null)
                    fingerprintListener.onAuthenticationSucceeded(result);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                if (fingerprintListener != null)
                    fingerprintListener.onAuthenticationFailed();
            }
        }, null);
    }

    public static void cancel() {
        if (cancellationSignal != null && !cancellationSignal.isCanceled())
            cancellationSignal.cancel();
    }

    public interface FingerprintListener {

        void onNonsupport();

        void onEnrollFailed();

        void onAuthenticationStart();

        void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result);

        void onAuthenticationFailed();

        void onAuthenticationError(int errMsgId, CharSequence errString);

        void onAuthenticationHelp(int helpMsgId, CharSequence helpString);
    }

    public abstract static class FingerprintListenerAdapter implements FingerprintListener {

        @Override
        public void onNonsupport() {
        }

        @Override
        public void onEnrollFailed() {
        }

        @Override
        public void onAuthenticationStart() {
        }

        @Override
        public void onAuthenticationFailed() {
        }

        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        }
    }
}
