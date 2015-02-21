package com.google.android.vending.remoting.api;

import android.accounts.Account;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.AndroidAuthenticator;
import com.google.android.finsky.protos.VendingProtos;
import com.google.android.finsky.utils.Maps;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;

import java.util.Locale;
import java.util.Map;

public class VendingApiContext {
    private final AndroidAuthenticator mAuthenticator;
    private final Context mContext;
    private boolean mHasPerformedInitialSecureTokenInvalidation;
    private boolean mHasPerformedInitialTokenInvalidation;
    private final Map<String, String> mHeaders = Maps.newHashMap();
    private String mLastAuthToken;
    private String mLastSecureAuthToken;
    private boolean mReauthenticate = false;
    private VendingProtos.RequestPropertiesProto mRequestProperties;
    private final AndroidAuthenticator mSecureAuthenticator;

    public VendingApiContext(Context context, Account account, Locale locale, String deviceId, int swVersion, String operatorName, String simOperatorName, String opeartorNumericName, String simOperatorNumericName, String productName, String productVer, String clientId, String loggingId) {
        this.mContext = context;
        this.mHeaders.put("User-Agent", "Android-Market/2");
        this.mAuthenticator = new AndroidAuthenticator(context, account, "android");
        this.mSecureAuthenticator = new AndroidAuthenticator(context, account, "androidsecure");
        this.mRequestProperties = new VendingProtos.RequestPropertiesProto();
        this.mRequestProperties.aid = deviceId;
        this.mRequestProperties.hasAid = true;
        this.mRequestProperties.userCountry = locale.getCountry();
        this.mRequestProperties.hasUserCountry = true;
        this.mRequestProperties.userLanguage = locale.getLanguage();
        this.mRequestProperties.hasUserLanguage = true;
        this.mRequestProperties.softwareVersion = swVersion;
        this.mRequestProperties.hasSoftwareVersion = true;
        if (operatorName != null) {
            this.mRequestProperties.operatorName = operatorName;
            this.mRequestProperties.hasOperatorName = true;
        }
        if (simOperatorName != null) {
            this.mRequestProperties.simOperatorName = simOperatorName;
            this.mRequestProperties.hasSimOperatorName = true;
        }
        if (opeartorNumericName != null) {
            this.mRequestProperties.operatorNumericName = opeartorNumericName;
            this.mRequestProperties.hasOperatorNumericName = true;
        }
        if (simOperatorNumericName != null) {
            this.mRequestProperties.simOperatorNumericName = simOperatorNumericName;
            this.mRequestProperties.hasSimOperatorNumericName = true;
        }
        this.mRequestProperties.productNameAndVersion = (productName + ":" + productVer);
        this.mRequestProperties.hasProductNameAndVersion = true;
        this.mRequestProperties.clientId = clientId;
        this.mRequestProperties.hasClientId = true;
        this.mRequestProperties.loggingId = loggingId;
        this.mRequestProperties.hasLoggingId = true;
    }

    public Account getAccount() {
        return this.mAuthenticator.getAccount();
    }

    public String getAuthToken()
            throws AuthFailureError {
        if (this.mReauthenticate) {
            this.mAuthenticator.invalidateAuthToken(this.mLastAuthToken);
            this.mReauthenticate = false;
        }
        this.mLastAuthToken = this.mAuthenticator.getAuthToken();
        return this.mLastAuthToken;
    }

    public Map<String, String> getHeaders() {
        return this.mHeaders;
    }

    public VendingProtos.RequestPropertiesProto getRequestProperties(boolean secure)
            throws AuthFailureError {
        if (((secure) && (!this.mHasPerformedInitialSecureTokenInvalidation)) || ((!secure) && (!this.mHasPerformedInitialTokenInvalidation))) {
            invalidateAuthToken(secure);
            this.mHasPerformedInitialSecureTokenInvalidation = true;
        }
        VendingProtos.RequestPropertiesProto localRequestPropertiesProto = new VendingProtos.RequestPropertiesProto();
        try {
            MessageNano.mergeFrom(localRequestPropertiesProto, MessageNano.toByteArray(this.mRequestProperties));
            String userAuthToken;
            if (secure) {
                userAuthToken=getSecureAuthToken();
            } else {
                userAuthToken=getAuthToken();
            }
            localRequestPropertiesProto.userAuthToken = (userAuthToken);
            localRequestPropertiesProto.hasUserAuthToken = true;
            localRequestPropertiesProto.userAuthTokenSecure = secure;
            localRequestPropertiesProto.hasUserAuthTokenSecure = true;
            this.mHasPerformedInitialTokenInvalidation = true;
        } catch (InvalidProtocolBufferNanoException localInvalidProtocolBufferNanoException) {
        }
        throw new IllegalStateException("Cannot happen.");
    }

    public String getSecureAuthToken()
            throws AuthFailureError {
        if (this.mReauthenticate) {
            this.mSecureAuthenticator.invalidateAuthToken(this.mLastAuthToken);
            this.mReauthenticate = false;
        }
        this.mLastSecureAuthToken = this.mSecureAuthenticator.getAuthToken();
        return this.mLastSecureAuthToken;
    }

    public void invalidateAuthToken(boolean secure) {
        if (secure) {
            this.mAuthenticator.invalidateAuthToken(this.mLastSecureAuthToken);
//            this.mLastSecureAuthToken = null;
        } else {
            this.mAuthenticator.invalidateAuthToken(this.mLastAuthToken);
//        this.mLastAuthToken = null;
        }
    }

    public void scheduleReauthentication(boolean paramBoolean) {
        this.mReauthenticate = true;
    }
}

/* Location:           /Users/alex/Documents/android-tools/com.android.vending-5.0.31-80300031-minAPI9-dex2jar.jar
 * Qualified Name:     com.google.android.vending.remoting.api.VendingApiContext
 * JD-Core Version:    0.6.2
 */