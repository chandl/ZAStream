package models;

import com.avaje.ebean.Model;
import play.Logger;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

/**
 * Password Recovery Table. Used to recover passwords.
 */
@Entity
@Table(name="password_recovery")
public class PasswordRecovery extends Model{

    public PasswordRecovery(User userToRecover) {
        this.userToRecover = userToRecover;

        String key = UUID.randomUUID().toString();
        key = key.replaceAll("-","");
        key = key.substring(0,24);

        PasswordRecovery recover;
        if((recover = findByUser(userToRecover)) != null){
            recover.delete(); //delete old password reset entries for this user.
        }

        recoveryHash = key;
    }

    @Id
    @GeneratedValue
    @Column(name="recoveryID")
    public int recoveryId;

    @Constraints.Required
    @Column(name="recoveryHash")
    public String recoveryHash;

    @OneToOne
    @Constraints.Required
    @Column(name="userToRecover")
    public User userToRecover;

    public static Finder find = new Finder(Integer.class, PasswordRecovery.class);

    public static PasswordRecovery findByHash(String hash){
        List<PasswordRecovery> recoveries = find.where().eq("recoveryHash", hash).findList();
        return (recoveries.size() == 0)? null:recoveries.get(0);
    }

    public static PasswordRecovery findByUser(User user){
        List<PasswordRecovery> recoveries = find.where().eq("userToRecover", user).findList();
        return (recoveries.size() == 0)? null:recoveries.get(0);
    }

    public int getRecoveryId() {
        return recoveryId;
    }

    public void setRecoveryId(int recoveryId) {
        this.recoveryId = recoveryId;
    }

    public String getRecoveryHash() {
        return recoveryHash;
    }

    public void setRecoveryHash(String recoveryHash) {
        this.recoveryHash = recoveryHash;
    }

    public User getUserToRecover() {
        return userToRecover;
    }

    public void setUserToRecover(User userToRecover) {
        this.userToRecover = userToRecover;
    }
}
