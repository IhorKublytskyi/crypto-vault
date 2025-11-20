package com.cryptovault.abstractions;

import java.io.IOException;

public interface IFileStorageService {
    String save(byte[] data) throws IOException;
    byte[] load(String path) throws IOException;
    void delete(String path) throws IOException;
}