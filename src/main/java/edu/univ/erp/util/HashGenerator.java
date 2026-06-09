package edu.univ.erp.util;

import org.mindrot.jbcrypt.BCrypt;

public class HashGenerator {
    public static void main(String[] args) {
        String password = "student"; // CHANGE THIS to test other passwords
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));

        System.out.println("Password: " + password);
        System.out.println("Generated Hash: " + hash);
        boolean matches = BCrypt.checkpw(password, hash);
        System.out.println("Verification Check: " + matches);
    }
}