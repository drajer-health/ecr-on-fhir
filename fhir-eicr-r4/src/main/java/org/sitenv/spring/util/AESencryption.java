package org.sitenv.spring.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

/**
 * AES helper for encrypting/decrypting short strings using a fixed,
 * hardcoded key, with results represented as Base64 text.
 */
public class AESencryption {

     private static final String ALGO = "AES";
    private static final byte[] keyValue =
        new byte[] { 'S', 'm', 'a', 'r', 't', 's', 't',
'S', 'e', 'c', 'r','e', 't', 'K', 'e', 'y' };

/**
 * Encrypts a plaintext string with AES and encodes the result as Base64.
 *
 * @param Data the plaintext to encrypt
 * @return the Base64-encoded ciphertext
 * @throws Exception if the cipher cannot be initialized or the encryption fails
 */
public static String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = Base64.getEncoder().encodeToString(encVal);
        return encryptedValue;
    }

    /**
     * Decrypts a Base64-encoded AES ciphertext produced by {@link #encrypt(String)}.
     *
     * @param encryptedData the Base64-encoded ciphertext
     * @return the decrypted plaintext
     * @throws Exception if the cipher cannot be initialized or decryption fails
     */
    public static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = Base64.getDecoder().decode(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    /**
     * @return the fixed AES key used by this class
     */
    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }

 }