package helper;

import org.mindrot.jbcrypt.BCrypt;

public class HashHelper {

    public static String createPassword(String clearText) {
        return BCrypt.hashpw(clearText, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPass, String encryptedPass){
     if(plainPass == null || encryptedPass == null) {return false;}

     return BCrypt.checkpw(plainPass,encryptedPass);
    }
}
