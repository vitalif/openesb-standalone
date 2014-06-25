package net.openesb.standalone.framework;

import net.openesb.standalone.security.utils.*;
import java.security.KeyStoreException;

/**
 *
 * @author David BRASSELY (brasseld at gmail.com)
 * @author OpenESB Community
 */
public class KeyStoreUtil implements com.sun.jbi.security.KeyStoreUtil {

    private final PasswordManagement manager;

    public KeyStoreUtil() {
        manager = new PasswordManagement();
    }

    /**
     * Encrypts a message using a default key.
     *
     * @param clearText the byte array that will be encrypted
     * @return the encrypted byte array
     * @exception KeyStoreException if any error occurs retrieving the key to be
     * used
     */
    @Override
    public byte[] encrypt(byte[] clearText) throws KeyStoreException {
        return manager.encrypt(clearText);
    }

    /**
     * Decrypts a message using a default key
     *
     * @param cipherText the byte array with the encrypted data
     * @return the unencrypted byte array
     * @exception KeyStoreException if any error occurs retrieving the key to be
     * used
     */
    @Override
    public byte[] decrypt(byte[] cipherText) throws KeyStoreException {
        return manager.decrypt(cipherText);
    }

    /**
     * Encrypts a message using a default key. The result is a Base64-encoded
     * string.
     *
     * @param clearText a String representing the message to be encrypted
     * @return a Base64-encoded string representing the encrypted message
     * @exception KeyStoreException if any error occurs retrieving the key to be
     * used
     */
    @Override
    public String encrypt(String clearText) throws KeyStoreException {
        return manager.encrypt(clearText);
    }

    /**
     * Decrypts a message using the key identified by keyName. The second
     * argument must be a Base-64 encoded string
     *
     * @param base64EncodedCipherText a Base-64 Encoded string
     * @return the decrypted message as a String
     * @exception KeyStoreException if any error occurs retrieving the key to be
     * used
     */
    @Override
    public String decrypt(String base64EncodedCipherText) throws KeyStoreException {
        return manager.decrypt(base64EncodedCipherText);
    }
}
