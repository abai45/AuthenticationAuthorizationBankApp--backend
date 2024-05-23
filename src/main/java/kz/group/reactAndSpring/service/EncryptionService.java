package kz.group.reactAndSpring.service;

public interface EncryptionService {
    String encrypt(String data);
    String decrypt(String encryptedData);
}
