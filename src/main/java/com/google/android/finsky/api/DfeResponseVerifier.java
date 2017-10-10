package com.google.android.finsky.api;

public interface DfeResponseVerifier
{
  String getSignatureRequest()
    throws DfeResponseVerifier.DfeResponseVerifierException;

  void verify(byte[] paramArrayOfByte, String paramString)
    throws DfeResponseVerifier.DfeResponseVerifierException;

  class DfeResponseVerifierException extends Exception
  {
    public DfeResponseVerifierException(String paramString)
    {
      super();
    }
  }
}