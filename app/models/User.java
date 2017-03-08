package models;

import com.avaje.ebean.Model;
import helper.HashHelper;
import helper.Secured;
import play.Logger;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "sender", targetEntity = Chat.class)
    private List<Chat> chatMessages = new ArrayList<Chat>();

    @OneToOne(mappedBy = "owner", targetEntity = Channel.class)
    public Channel userChannel;

    public User(String userName, String passWord, String email) {
        this.userName = userName;
        this.passWord = HashHelper.createPassword(passWord);
        this.email = email;
    }

    public static Finder find = new Finder(Integer.class, User.class);

    public List<ValidationError> validate(){
        List<ValidationError> errors = new ArrayList<ValidationError>();

        if(Secured.emailExists(email)){
            errors.add(new ValidationError("email", "Sorry, this e-mail is already registered."));
        }

        if(Secured.isUser(userName)){
            errors.add(new ValidationError("userName", "Sorry, this username is already registered."));
        }

        return errors.isEmpty()? null : errors;
    }

    public static User findByUsername(String userName){
        List<User> users = find.where().eq("userName", userName).findList();
        return (users.size() == 0)? null:users.get(0);
    }

    public Channel findChannel(){
        List<Channel> theChannel = Channel.find.where().eq("userID", this.userId).findList();

        if(theChannel.size() == 0) {
            Logger.debug("No Channel Found for ID: "+userId + ", Name: "+userName);
            return null;
        }else{
            Logger.debug("Channel Found for ID: "+userId + ", Name: "+userName+", Key: "+theChannel.get(0).getStreamKey());
            return theChannel.get(0);
        }
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

    public static Finder getFind() {
        return find;
    }

    public static void setFind(Finder find) {
        User.find = find;
    }
}
