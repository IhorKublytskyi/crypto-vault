package com.cryptovault.datatransferobjects;

public class EncryptedData {
    private final byte[] _ciphertext;
    private final byte[] _iv;
    private final byte[] _tag;

    public EncryptedData(byte[] ciphertext, byte[] iv, byte[] tag){
        _ciphertext = ciphertext;
        _iv = iv;
        _tag = tag;
    }

    public byte[] getCiphertext() { return _ciphertext;}
    public byte[] getIv() { return _iv; }
    public byte[] getTag() { return _tag; }
}
