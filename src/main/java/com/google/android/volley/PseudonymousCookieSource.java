package com.google.android.volley;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.EntityEnclosingRequestWrapper;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.message.BasicHeader;

public abstract interface PseudonymousCookieSource {
    public abstract String getCookieName();

    public abstract String getCookieValue();

    public abstract void setCookieValue(String paramString);

    public static final class Helper {
        private static final Pattern COOKIE_PAIR = Pattern.compile("(^|[\\s;,]+)([^()<>@,;:\\\"/\\[\\]\\?={}\\s]+)\\s*=\\s*(\"[^\"]*\"|[^,;\\s\"]+)");
        private static final int DOT = ".".codePointAt(0);
        private static final String[] PSEUDONYMOUS_ID_DOMAINS = {"google.com", "googleapis.com"};
        private static final Pattern TOKEN = Pattern.compile("[^()<>@,;:\\\"/\\[\\]\\?={}\\s]+");
        private static final Pattern VALUE = Pattern.compile("[^,;\\s\"]+");

        public static boolean cookieSourceApplies(HttpUriRequest paramHttpUriRequest, PseudonymousCookieSource paramPseudonymousCookieSource) {
            if ((paramPseudonymousCookieSource == null) || (!"HTTPS".equalsIgnoreCase(paramHttpUriRequest.getURI().getScheme()))) {
                return false;
            }
            String[] arrayOfString = PSEUDONYMOUS_ID_DOMAINS;
            int i = arrayOfString.length;
            for (int j = 0; j < i; j++) {
                if (domainMatches(arrayOfString[j], paramHttpUriRequest.getURI().getHost()))
                    return true;
            }
            return false;
        }

        public static boolean domainMatches(String domain1, String domain2) {
            if (domain1.length() > domain2.length()) {
                return false;
            }
            if (domain1.equalsIgnoreCase(domain2)) {
                return true;
            }
            return false;
        }

        public static String getCookieValue(String paramString1, String paramString2) {
            Matcher localMatcher = COOKIE_PAIR.matcher(paramString1);
            while (localMatcher.find())
                if (localMatcher.group(2).equals(paramString2)) {
                    String str1 = localMatcher.group();
                    String str2 = str1.substring(1 + str1.indexOf("=")).trim();
                    if (str2.startsWith("\""))
                        str2 = str2.substring(1, -1 + str2.length());
                    return str2;
                }
            return null;
        }

        public static boolean isCookiePresent(String paramString1, String paramString2) {
            Matcher localMatcher = COOKIE_PAIR.matcher(paramString1);
            while (localMatcher.find())
                if (localMatcher.group(2).equals(paramString2))
                    return true;
            return false;
        }

        public static String replaceCookie(String paramString1, String paramString2, String paramString3) {
            Matcher localMatcher = COOKIE_PAIR.matcher(paramString1);
            StringBuffer localStringBuffer = new StringBuffer();
            while (localMatcher.find())
                if (localMatcher.group(2).equals(paramString2))
                    localMatcher.appendReplacement(localStringBuffer, localMatcher.group(1) + paramString2 + "=" + wrapInQuotesIfNeeded(paramString3));
                else
                    localMatcher.appendReplacement(localStringBuffer, localMatcher.group());
            localMatcher.appendTail(localStringBuffer);
            return localStringBuffer.toString();
        }

        public static HttpUriRequest setRequestCookie(HttpUriRequest paramHttpUriRequest, PseudonymousCookieSource paramPseudonymousCookieSource)
                throws IOException {
            if (cookieSourceApplies(paramHttpUriRequest, paramPseudonymousCookieSource)) {
                String str1 = paramPseudonymousCookieSource.getCookieName();
                String str2 = paramPseudonymousCookieSource.getCookieValue();
                int i = 0;
                for (Header localHeader : paramHttpUriRequest.getHeaders("Cookie"))
                    if (isCookiePresent(localHeader.getValue(), str1)) {
                        if ((i == 0) && (!(paramHttpUriRequest instanceof RequestWrapper)))
                            paramHttpUriRequest = wrapRequest(paramHttpUriRequest);
                        i = 1;
                        paramHttpUriRequest.removeHeader(localHeader);
                        paramHttpUriRequest.addHeader(new BasicHeader(localHeader.getName(), replaceCookie(localHeader.getValue(), str1, str2)));
                    }
                if (i == 0)
                    paramHttpUriRequest.addHeader(new BasicHeader("Cookie", str1 + "=" + wrapInQuotesIfNeeded(str2)));
            }
            return paramHttpUriRequest;
        }

        public static HttpResponse updateFromResponseCookie(HttpUriRequest paramHttpUriRequest, HttpResponse paramHttpResponse, PseudonymousCookieSource paramPseudonymousCookieSource) {
            if (cookieSourceApplies(paramHttpUriRequest, paramPseudonymousCookieSource)) {
                String str1 = paramPseudonymousCookieSource.getCookieName();
                Header[] arrayOfHeader1 = paramHttpResponse.getHeaders("Set-Cookie");
                int i = arrayOfHeader1.length;
                for (int j = 0; j < i; j++) {
                    String str3 = getCookieValue(arrayOfHeader1[j].getValue(), str1);
                    if (str3 != null)
                        paramPseudonymousCookieSource.setCookieValue(str3);
                }
                Header[] arrayOfHeader2 = paramHttpResponse.getHeaders("Set-Cookie2");
                int k = arrayOfHeader2.length;
                for (int m = 0; m < k; m++) {
                    String str2 = getCookieValue(arrayOfHeader2[m].getValue(), str1);
                    if (str2 != null)
                        paramPseudonymousCookieSource.setCookieValue(str2);
                }
            }
            return paramHttpResponse;
        }

        public static String wrapInQuotesIfNeeded(String paramString) {
            if (paramString == null)
                paramString = "\"\"";
            if (VALUE.matcher(paramString).matches()) {
                return paramString;
            }
            return "\"" + paramString + "\"";
        }

        private static RequestWrapper wrapRequest(HttpUriRequest paramHttpUriRequest)
                throws IOException {
            try {
                RequestWrapper localObject;
                if ((paramHttpUriRequest instanceof HttpEntityEnclosingRequest)) {
                    localObject = new EntityEnclosingRequestWrapper((HttpEntityEnclosingRequest) paramHttpUriRequest);
                } else {
                    localObject = new RequestWrapper(paramHttpUriRequest);
                }
                localObject.resetHeaders();
                return localObject;
            } catch (ProtocolException localProtocolException) {
                throw new ClientProtocolException(localProtocolException);
            }
        }
    }
}