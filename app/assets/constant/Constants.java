package assets.constant;

/**
 * Constants: Holds Constant Variables used Throughout the Program.
 *
 * @author Chandler Severson
 * @author Yiwei Zheng
 * @version 2.0
 * @since 1.0
 */
public class Constants {
    public static final String USERNAME_PLACEHOLDER = "Username";
    public static final String PASSWORD_PLACEHOLDER = "Password";
    public static final String EMAIL_PLACEHOLDER = "Email";
    public static final String HOSTNAME = play.Play.application().configuration().getString("my.HOSTNAME");
    public static final String RECOVERY_LINK = "https://"+HOSTNAME+"/recover-password/";
}
