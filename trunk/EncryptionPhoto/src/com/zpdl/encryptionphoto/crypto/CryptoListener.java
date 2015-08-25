package com.zpdl.encryptionphoto.crypto;

public interface CryptoListener {
    public void onCryptoProgress(String msg, int count, int percent);
    public void onCryptoComplete(int result, String[] in, String[] out);
}
