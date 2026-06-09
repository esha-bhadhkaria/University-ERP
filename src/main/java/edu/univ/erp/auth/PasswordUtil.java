package edu.univ.erp.auth;

public class PasswordUtil {
    //Hashes a plaintext password using BCrypt.
    public static String hashPassword(String plaintextPassword) {
        return BcryptHasher.hashPassword(plaintextPassword);
    }
    public static boolean verifyPassword(String plaintextPassword, String storedHash) {
        return BcryptHasher.checkPassword(plaintextPassword, storedHash);
    }
}