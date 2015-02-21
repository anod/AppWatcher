package com.google.android.finsky.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

public class Utils
{
  private static Pattern COMMA_PATTERN = Pattern.compile(",");
  private static String[] EMPTY_ARRAY = new String[0];

//  public static String commaPackStrings(String[] paramArrayOfString)
//  {
//    if ((paramArrayOfString == null) || (paramArrayOfString.length == 0))
//      return "";
//    if (paramArrayOfString.length == 1)
//      return paramArrayOfString[0];
//    StringBuilder localStringBuilder = new StringBuilder();
//    for (int i = 0; i < paramArrayOfString.length; i++)
//    {
//      if (i != 0)
//        localStringBuilder.append(',');
//      localStringBuilder.append(paramArrayOfString[i]);
//    }
//    return localStringBuilder.toString();
//  }
//
//  public static String[] commaUnpackStrings(String paramString)
//  {
//    if ((paramString == null) || (paramString.length() == 0))
//      return EMPTY_ARRAY;
//    if (paramString.indexOf(',') == -1)
//      return new String[] { paramString };
//    return COMMA_PATTERN.split(paramString);
//  }

  public static void copy(InputStream paramInputStream, OutputStream paramOutputStream)
    throws IOException
  {
    byte[] arrayOfByte = new byte[4096];
    try
    {
      while (true)
      {
        int i = paramInputStream.read(arrayOfByte);
        if (i == -1)
          break;
        paramOutputStream.write(arrayOfByte, 0, i);
      }
    }
    finally
    {
      paramInputStream.close();
    }
    paramInputStream.close();
  }
//
//  public static void ensureNotOnMainThread()
//  {
//    if (Looper.myLooper() != Looper.getMainLooper())
//      return;
//    throw new IllegalStateException("This method cannot be called from the UI thread.");
//  }
//
//  public static void ensureOnMainThread()
//  {
//    if (Looper.myLooper() == Looper.getMainLooper())
//      return;
//    throw new IllegalStateException("This method must be called from the UI thread.");
//  }
//
//  public static <P> void executeMultiThreaded(AsyncTask<P, ?, ?> paramAsyncTask, P[] paramArrayOfP)
//  {
//    if (Build.VERSION.SDK_INT >= 11)
//    {
//      paramAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, paramArrayOfP);
//      return;
//    }
//    paramAsyncTask.execute(paramArrayOfP);
//  }
//
//  public static String getItalicSafeString(String paramString)
//  {
//    return paramString + " ";
//  }
//
//  public static String getPreferenceKey(String paramString1, String paramString2)
//  {
//    return paramString2 + ":" + paramString1;
//  }
//
//  public static boolean isBackgroundDataEnabled(Context paramContext)
//  {
//    ConnectivityManager localConnectivityManager = (ConnectivityManager)paramContext.getSystemService("connectivity");
//    if (Build.VERSION.SDK_INT < 14)
//      return localConnectivityManager.getBackgroundDataSetting();
//    for (NetworkInfo localNetworkInfo : localConnectivityManager.getAllNetworkInfo())
//      if ((localNetworkInfo != null) && (localNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.BLOCKED))
//        return false;
//    return true;
//  }
//
//  public static boolean isEmptyOrSpaces(CharSequence paramCharSequence)
//  {
//    return (paramCharSequence == null) || (paramCharSequence.length() == 0) || (trim(paramCharSequence).length() == 0);
//  }
//
    public static byte[] readBytes(InputStream paramInputStream)
            throws IOException {
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
        try {
            copy(paramInputStream, localByteArrayOutputStream);
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            return arrayOfByte;
        } finally {
            localByteArrayOutputStream.close();
        }
    }

//  public static void safeClose(Closeable paramCloseable)
//  {
//    if (paramCloseable != null);
//    try
//    {
//      paramCloseable.close();
//      return;
//    }
//    catch (IOException localIOException)
//    {
//    }
//  }

//  public static void syncDebugActivityStatus(Context paramContext)
//  {
//    PackageManager localPackageManager = paramContext.getPackageManager();
//    if (((Boolean)G.debugOptionsEnabled.get()).booleanValue());
//    for (int i = 1; ; i = 2)
//    {
//      //localPackageManager.setComponentEnabledSetting(new ComponentName(paramContext, DebugActivity.class), i, 1);
//      return;
//    }
//  }

//  public static CharSequence trim(CharSequence paramCharSequence)
//  {
//    if (paramCharSequence == null)
//      paramCharSequence = null;
//    int i;
//    int j;
//    int k;
//    do
//    {
//      return paramCharSequence;
//      i = 0;
//      j = -1 + paramCharSequence.length();
//      k = j;
//      while ((i <= k) && (paramCharSequence.charAt(i) <= ' '))
//        i++;
//      while ((k >= i) && (paramCharSequence.charAt(k) <= ' '))
//        k--;
//    }
//    while ((i == 0) && (k == j));
//    return paramCharSequence.subSequence(i, k + 1);
//  }

//  public static String urlDecode(String paramString)
//  {
//    try
//    {
//      String str = URLDecoder.decode(paramString, "UTF-8");
//      return str;
//    }
//    catch (IllegalArgumentException localIllegalArgumentException)
//    {
//      Object[] arrayOfObject = new Object[2];
//      arrayOfObject[0] = paramString;
//      arrayOfObject[1] = localIllegalArgumentException.getMessage();
//      FinskyLog.d("Unable to parse %s - %s", arrayOfObject);
//      return null;
//    }
//    catch (UnsupportedEncodingException localUnsupportedEncodingException)
//    {
//      FinskyLog.wtf("%s", new Object[] { localUnsupportedEncodingException });
//      throw new RuntimeException(localUnsupportedEncodingException);
//    }
//  }

//  public static String urlEncode(String paramString)
//  {
//    try
//    {
//      String str = URLEncoder.encode(paramString, "UTF-8");
//      return str;
//    }
//    catch (UnsupportedEncodingException localUnsupportedEncodingException)
//    {
//      FinskyLog.wtf("%s", new Object[] { localUnsupportedEncodingException });
//      throw new RuntimeException(localUnsupportedEncodingException);
//    }
//  }
}

/* Location:           /Users/alex/Documents/android-tools/com.android.vending-5.0.31-80300031-minAPI9-dex2jar.jar
 * Qualified Name:     com.google.android.finsky.utils.Utils
 * JD-Core Version:    0.6.2
 */