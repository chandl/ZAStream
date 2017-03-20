package models;

import com.avaje.ebean.Model;
import helper.HashHelper;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Data Model + Helper Methods for Users
 *
 * @author Chandler Severson <seversonc@sou.edu>
 * @author Yiwei Zheng <zhengy1@sou.edu>
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name="user")
public class User extends Model{

    @Id
    @GeneratedValue
    @Column(name="userID")
    public int userId;

    @Constraints.Required
    @Size(max=50)
    @Column(name="userName")
    public String userName;

    @Size(min=6, max=64)
    @Constraints.Required
    @Column(name="passWord")
    public String passWord;

    @Constraints.Email
    @Constraints.Required
    @Size(max=64)
    @Column(name="email")
    public String email;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    /**
     * The One-to-Many relationship between the {@link User} and their {@link Chat} messages.
     */
    @OneToMany(mappedBy = "sender", targetEntity = Chat.class)
    private List<Chat> chatMessages = new ArrayList<Chat>();

    /**
     * The One-To-One relationship between the {@link User} and their {@link Channel}
     */
    @OneToOne(mappedBy = "owner", targetEntity = Channel.class)
    public Channel userChannel;

    /**
     * Create a new User.
     *
     * @param userName the user's identifier.
     * @param passWord the user's password.
     * @param email the user's email.
     */
    public User(String userName, String passWord, String email) {
        this.userName = userName;
        this.passWord = HashHelper.createPassword(passWord);
        this.email = email;
    }

    /**
     * The {@link com.avaje.ebean.Model.Finder} method to find entries in the DB.
     */
    public static Finder find = new Finder(Integer.class, User.class);

    /**
     * Checks if a userName is in the DB.
     *
     * @param name the userName to check.
     * @return <code>true</code> if the userName is found in the database.
     */
    public static boolean isUser(String name){
        return findByUsername(name) != null;
    }

    /**
     * Checks if a email is in the DB
     *
     * @param mail the email to check.
     * @return <code>true</code> if the email is found in the database.
     */
    public static boolean emailExists(String mail){
        return findByEmail(mail) != null;
    }

    /**
     * Validation method to check if a user entered the correct password while logging in.
     *
     * <p>
     *     Uses {@link HashHelper} to securely check the password vs. encrypted password hashes.
     * </p>
     *
     * @param userName The username.
     * @param passWord The password.
     * @return <code>true</code> if the password matches what is in the DB.
     */
    public static boolean checkPassword(String userName, String passWord){
        User user = findByUsername(userName);
        return HashHelper.checkPassword(passWord, user.getPassWord());
    }

    /**
     * Verifies that a {@link User} with the specified parameters is valid.
     *
     * @param userName The username to check.
     * @param password The password to check.
     * @return <code>true</code> if the account&password is valid, <code>false</code> otherwise.
     */
    public static boolean isValid(String userName, String password){

        return (userName != null && password != null) && isUser(userName) && checkPassword(userName, password);
    }

    /**
     * Helper method to validate email and password entries in registration.
     *
     * <p>
     *     Checks the database to make sure the specified email and password have not been previously used.
     * </p>
     *
     * @return <code>null</code> if there are no errors, {@link List<ValidationError>} of errors otherwise.
     */
    public List<ValidationError> validate(){
        List<ValidationError> errors = new ArrayList<ValidationError>();

        Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
        Matcher emailMatcher = emailPattern.matcher(email);

        Pattern specialPattern = Pattern.compile("[^A-Za-z0-9-_]");
        Matcher specialMatcher = specialPattern.matcher(userName);


        if(!specialMatcher.matches()){
            errors.add(new ValidationError("userNameInvalid", "Sorry, you cannot have special characters in your username (Except '-' and '_')."));
        }

        if(!emailMatcher.matches()){
            errors.add(new ValidationError("emailInvalid", "Invalid e-mail entered."));
        }

        if(emailExists(email)){
            errors.add(new ValidationError("emailRegistered", "Sorry, this e-mail is already registered."));
        }

        if(isUser(userName)){
            errors.add(new ValidationError("userName", "Sorry, this username is already registered."));
        }

        return errors.isEmpty()? null : errors;
    }

    /**
     * Finds a {@link User} in the databse by their userName.
     *
     * @param userName The userName of the desired {@link User}
     * @return <code>null</code> if the userName is not found in the database, otherwise return the matching {@link User}.
     */
    public static User findByUsername(String userName){
        List<User> users = find.where().eq("userName", userName).findList();
        return (users.size() == 0)? null:users.get(0);
    }

    /**
     * Finds a {@link User} in the database by their email.
     *
     * @param email The email of the desired {@link User}
     * @return <code>null</code> if the email is not found in the database, otherwise return the matching {@link User}.
     */
    public static User findByEmail(String email){
        List<User> users = find.where().eq("email",email).findList();
        return(users.size() == 0)? null:users.get(0);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("User: ");
        sb.append("{id=").append(userId);
        sb.append(", name=").append(userName);
        sb.append(", email=").append(email);
        sb.append("}");

        return sb.toString();
    }

    //====================Getters and Setters====================
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Chat> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<Chat> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public Channel getUserChannel() {
        return userChannel;
    }

    public void setUserChannel(Channel userChannel) {
        this.userChannel = userChannel;
    }
    //====================END Getters and Setters====================
}
