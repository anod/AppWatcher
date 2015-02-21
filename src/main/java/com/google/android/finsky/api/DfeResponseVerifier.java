package com.google.android.finsky.api;

public abstract interface DfeResponseVerifier
{
  public abstract String getSignatureRequest()
    throws DfeResponseVerifier.DfeResponseVerifierException;

  public abstract void verify(byte[] paramArrayOfByte, String paramString)
    throws DfeResponseVerifier.DfeResponseVerifierException;

  public static class DfeResponseVerifierException extends Exception
  {
    public DfeResponseVerifierException(String paramString)
    {
      super();
    }
  }
}