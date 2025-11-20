package com.cryptovault.utils;

public class CryptoUtilsConstants {
    public static final int AES_KEY_SIZE = 256;
    public static final int RSA_KEY_SIZE = 3072;
    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 128;

    public static final String AES_ALGORITHM = "AES";
    public static final String AES_GCM_TRANSFORM = "AES/GCM/NoPadding";
    public static final String RSA_TRANSFORM = "RSA/OAEPWithSHA-256AndMGF1Padding";
    public static final String RSA_ALGORITHM = "RSA";
}
