package com.google.android.finsky.api;

import org.keyczar.interfaces.*;
import android.content.*;
import java.io.*;
import java.security.*;
import android.text.*;
import android.util.*;

import com.anod.appwatcher.BuildConfig;
import com.google.android.finsky.api.utils.AndroidKeyczarReader;

import org.keyczar.*;
import org.keyczar.exceptions.*;

public class DfeResponseVerifierImpl implements DfeResponseVerifier
{
    private static final String FALLBACK_KEYS_FILES_SUBDIR;
    private static final String PROD_KEYS_ASSETS_SUBDIR;
    private static SecureRandom SECURE_RANDOM;
    private static KeyczarReader sFallbackReader;
    private static boolean sFallbackReaderInitialized;
    private static KeyczarReader sProdReader;
    private final Context mContext;
    private byte[] mNonce;
    private boolean mNonceInitialized;
    
    static {
        PROD_KEYS_ASSETS_SUBDIR = "keys" + File.separator + "dfe-response-auth";
        FALLBACK_KEYS_FILES_SUBDIR = "keys" + File.separator + "dfe-response-auth-dev";
        try {
            SECURE_RANDOM = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException ex) {
            AppLog.e("Could not initialize SecureRandom, SHA1PRNG not supported. %s", ex);
        }
    }
    
    public DfeResponseVerifierImpl(final Context mContext) {
        super();
        this.mNonce = new byte[256];
        this.mContext = mContext;
    }
    
    private static byte[] extractResponseSignature(final String s) throws DfeResponseVerifierException {
        if (TextUtils.isEmpty((CharSequence)s)) {
            AppLog.e("No signing response found.", new Object[0]);
            throw new DfeResponseVerifierException("No signing response found.");
        }
        final String[] split = s.split(";");
        for (int length = split.length, i = 0; i < length; ++i) {
            final String trim = split[i].trim();
            if (trim.startsWith("signature=")) {
                return Base64.decode(trim.substring(10), 11);
            }
        }
        throw new DfeResponseVerifierException("Signature not found in response: " + s);
    }
    
    private static KeyczarReader getFallbackReader(final Context context) {
        synchronized (DfeResponseVerifierImpl.class) {
            if (!DfeResponseVerifierImpl.sFallbackReaderInitialized) {
                final File file = new File(context.getFilesDir(), DfeResponseVerifierImpl.FALLBACK_KEYS_FILES_SUBDIR);
                if (file.exists()) {
                    DfeResponseVerifierImpl.sFallbackReader = new KeyczarFileReader(file.getAbsolutePath());
                }
                DfeResponseVerifierImpl.sFallbackReaderInitialized = true;
            }
            return DfeResponseVerifierImpl.sFallbackReader;
        }
    }
    
    private static KeyczarReader getProdReader(final Context context) {
        synchronized (DfeResponseVerifierImpl.class) {
            if (DfeResponseVerifierImpl.sProdReader == null) {
                DfeResponseVerifierImpl.sProdReader = new AndroidKeyczarReader(context.getResources(), DfeResponseVerifierImpl.PROD_KEYS_ASSETS_SUBDIR);
            }
            return DfeResponseVerifierImpl.sProdReader;
        }
    }
    
    private boolean internalVerify(final KeyczarReader keyczarReader, final byte[] array, final byte[] array2, final String s) throws DfeResponseVerifierException {
        try {
            final Verifier verifier = new Verifier(keyczarReader);
            final byte[] array3 = new byte[array.length + array2.length];
            System.arraycopy(array, 0, array3, 0, array.length);
            System.arraycopy(array2, 0, array3, array.length, array2.length);
            return verifier.verify(array3, extractResponseSignature(s));
        }
        catch (KeyczarException ex) {
            AppLog.d("Keyczar exception during signature verification: %s", ex);
            return false;
        }
    }
    
    @Override
    public String getSignatureRequest() throws DfeResponseVerifierException {
        synchronized (this) {
            if (DfeResponseVerifierImpl.SECURE_RANDOM == null) {
                throw new DfeResponseVerifierException("Uninitialized SecureRandom.");
            }
        }
        if (!this.mNonceInitialized) {
            DfeResponseVerifierImpl.SECURE_RANDOM.nextBytes(this.mNonce);
            this.mNonceInitialized = true;
        }
        // monitorexit(this)
        return "nonce=" + Base64.encodeToString(this.mNonce, 11);
    }
    
    @Override
    public void verify(final byte[] array, final String s) throws DfeResponseVerifierException {
        boolean b = this.internalVerify(getProdReader(this.mContext), this.mNonce, array, s);
        if (!b) {
            final KeyczarReader fallbackReader = getFallbackReader(this.mContext);
            if (fallbackReader != null) {
                AppLog.d("Retry verification using fallback keys.", new Object[0]);
                b = this.internalVerify(fallbackReader, this.mNonce, array, s);
            }
        }
        if (!b || BuildConfig.DEBUG) {
            AppLog.d("Response signature verified: %b", b);
        }
        if (!b) {
            throw new DfeResponseVerifierException("Response signature mismatch.");
        }
    }
}
