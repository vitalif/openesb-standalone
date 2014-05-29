package net.openesb.standalone.security.utils;

import java.security.KeyStoreException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class KeyStoreUtil implements com.sun.jbi.security.KeyStoreUtil {

    private final BASE64Encoder mBase64Encoder;
    private final BASE64Decoder mBase64Decoder;
    
    private final static String encryptionKey = "A12EF89A23C6A5B7";
    private final static String IV = "A12EF89A23C6A5B7";
    
    public KeyStoreUtil() {
        mBase64Encoder = new BASE64Encoder();
        mBase64Decoder = new BASE64Decoder();
    }
    
    /**
     * Encrypts a message using a default key. 
     *
     * @param        clearText the byte array that will be encrypted
     * @return       the encrypted byte array
     * @exception    KeyStoreException if any error occurs retrieving the
     * key to be used
     */
    @Override
    public byte[] encrypt(byte[] clearText) throws KeyStoreException {
        try {
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
            
            // Create the cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            
            // Initialize the cipher for encryption
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));
            
            // Encrypt the cleartext
            byte[] cipherText = cipher.doFinal(clearText);
            
            return cipherText;
        } catch (Exception ex) {
            throw new KeyStoreException(ex);
        }
    }

    /**
     * Decrypts a message using a default key
     *
     * @param        cipherText the byte array with the encrypted data
     * @return       the unencrypted byte array
     * @exception    KeyStoreException if any error occurs retrieving the
     * key to be used
     */
    @Override
    public byte[] decrypt(byte[] cipherText) throws KeyStoreException {
        try {
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
            
            // Create the cipher 
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        
            // Initialize the cipher for decryption
            cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        
            // Decrypt the ciphertext
            byte[] cleartext = cipher.doFinal(cipherText);

            return cleartext;
        } catch (Exception ex) {
            throw new KeyStoreException(ex);
        }
    }

    /**
     * Encrypts a message using a default key.  The result
     * is a Base64-encoded string.
     *
     * @param        clearText a String representing the message to be encrypted
     * @return       a Base64-encoded string representing the encrypted message
     * @exception    KeyStoreException if any error occurs retrieving the
     * key to be used
     */
    @Override
    public String encrypt(String clearText) throws KeyStoreException {
        try {
            byte[] cipherText = encrypt(clearText.getBytes());
            return mBase64Encoder.encode(cipherText);
        } catch (Exception ex) {
            throw new KeyStoreException(ex);
        }
    }

    /**
     * Decrypts a message using the key identified by keyName.  The second
     * argument must be a Base-64 encoded string
     *
     * @param        base64EncodedCipherText a Base-64 Encoded string
     * @return       the decrypted message as a String
     * @exception    KeyStoreException if any error occurs retrieving the
     * key to be used
     */
    @Override
    public String decrypt(String base64EncodedCipherText) throws KeyStoreException {
        try {
            byte[] clearText = decrypt(mBase64Decoder.decodeBuffer(base64EncodedCipherText));
            return new String(clearText);
        } catch (Exception ex) {
            throw new KeyStoreException(ex);
        }
    }
}
