package models;

import com.avaje.ebean.Model;
import play.Logger;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * PasswordRecovery: Model for Password Recovery
 *
 * @author Chandler Severson
 * @author Yiwei Zheng
 * @version 2.0
 * @since 2.0
 */
@Entity
@Table(name="password_recovery")
public class PasswordRecovery extends Model{

    /**
     * Method to create a Password Recovery object / DB entry.
     * 
     * This will create a random 24-digit string that is linked to a specific channel for a limited time (2 hrs).
     * If a user accesses the password recovery page with this 24-digit recovery string, they will be able to reset
     * their password.
     * 
     * @param userToRecover The {@link User} to create a PasswordRecovery entry for.
     */
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

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, 2);
        Date date = cal.getTime();

        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        Time time = new Time(date.getTime());

        expireDate = sqlDate;
        expireTime = time;
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

    @Column(name="expireDate")
    public java.sql.Date expireDate;

    @Column(name="expireTime")
    public Time expireTime;

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

    /**
     * Method that check if the password recovery is expired.
     * @return {code true} if is expired and {@code false} if is not expired.
     */
    public boolean isExpired(){
        Date currentDate = new Date();
        Date sqlExpireDate = new Date(expireDate.getTime());

        //Create calendar with Expire Date
        Calendar cal = Calendar.getInstance();
        cal.setTime(sqlExpireDate);

        //Create calendar with Expire Time
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(expireTime);

        //Set the correct time in the Calendar with the correct Expire date.
        cal.set(Calendar.MINUTE, cal1.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal1.get(Calendar.SECOND));
        cal.set(Calendar.HOUR_OF_DAY, cal1.get(Calendar.HOUR_OF_DAY));

        //Update the expire date with the first calendar. (Now it contains date AND time)
        sqlExpireDate = cal.getTime();

        if(currentDate.after(sqlExpireDate)){
            Logger.debug(String.format("Password Recovery Expired. %s. RecoveryHash: %s. Expire Time: %s %s", getUserToRecover(), getRecoveryHash(), getExpireDate().toString() , getExpireTime().toString()));

            this.delete(); //delete PasswordRecovery entry from the database.
            return true;
        }

        return false;
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

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(java.sql.Date expireDate) {
        this.expireDate = expireDate;
    }

    public Time getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Time expireTime) {
        this.expireTime = expireTime;
    }
}
