package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;

/*class for hashing and verifying passwords using the BCrypt algorithm.*/
public class BcryptHasher {
    private static final int LOG_ROUNDS = 10;
    //Hashes a plaintext password using BCrypt.
    public static String hashPassword(String plaintextPassword) {
        if (plaintextPassword == null || plaintextPassword.isEmpty()) {
            return null;
        }
        String salt = BCrypt.gensalt(LOG_ROUNDS);
        return BCrypt.hashpw(plaintextPassword, salt);
    }
     //Verifies a plaintext password against a stored hashed password.
    public static boolean checkPassword(String plaintextPassword, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plaintextPassword, storedHash);
        } catch (IllegalArgumentException e) {
            System.err.println("Error verifying password hash: Invalid hash format. " + e.getMessage());
            return false;
        }
    }
}