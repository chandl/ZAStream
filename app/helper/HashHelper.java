package helper;

import org.mindrot.jbcrypt.BCrypt;

/**
 * HashHelper - Class to create and validate encrypted passwords.
 * Using {@link org.mindrot.jbcrypt.BCrypt} to encrypt.
 *
 * @author Chandler Severson <seversonc@sou.edu>
 * @version 1.0
 * @since 1.0
 */
public class HashHelper {

    /**
     * Encrypts a clearText password with {@link BCrypt}
     *
     * @param clearText The clearText password to be encrypted.
     * @return A BCrypt encrypted version of the clearText.
     */
    public static String createPassword(String clearText) {
        return BCrypt.hashpw(clearText, BCrypt.gensalt());
    }

    /**
     * Checks a plainText password against the encrypted version using {@link BCrypt}.
     *
     * @param plainPass The plainText password to check.
     * @param encryptedPass The BCrypt Hash of the valid password.
     * @return true if the password matches, false otherwise.
     */
    public static boolean checkPassword(String plainPass, String encryptedPass){
     if(plainPass == null || encryptedPass == null) {return false;}

     return BCrypt.checkpw(plainPass,encryptedPass);
    }
}
