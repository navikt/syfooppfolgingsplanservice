package no.nav.syfo.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class DigestUtil {

    public static String sha512AsBase64String(byte[] source) {
        return Base64.getEncoder().encodeToString(sha512AsByteArray(source));
    }

    public static byte[] sha512AsByteArray(byte[] source) {
        try {
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            return sha512.digest(source);
        } catch (NoSuchAlgorithmException  e) {
            throw new RuntimeException("Feil ved generering av hash.", e);
        }
    }
}
