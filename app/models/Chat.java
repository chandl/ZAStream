package models;

import com.avaje.ebean.Model;
import org.joda.time.LocalDate;
import play.data.format.Formats;
import javax.persistence.*;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name="chat")
public class Chat extends Model {

    @Id
    @GeneratedValue
    @Column(name="chatID")
    public int chatId;

    @ManyToOne
    @JoinColumn(name="senderID")
    public User sender;

    @ManyToOne
    @JoinColumn(name="roomID")
    public ChatRoom chatRoom;


    @Column(name="sentDate")
    public java.sql.Date sentDate;

    @Column(name="sentTime")
    public Time sentTime;

    @Column(name="message")
    public String message;

    public static Finder<Integer, Chat> find = new Finder<Integer, Chat>(Chat.class);


    public Chat(User sender, ChatRoom room, String message){
        this.sender = sender;
        this.chatRoom = room;
        this.message = message;
        Date date = new Date();

        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        Time now = new Time(date.getTime());
        sentDate = sqlDate;
        sentTime = now;
    }

    public String getSentTime(){
        if(sentTime == null) return null;
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfDate.format(sentTime);

        return sdfDate.toString();
    }

    @Override
    public String toString() {
        return "{\"time\":\"" + sentTime +
                "\". \"sender\"\""+sender
                +"\", \"message\":\""+ message+ "\"}";
    }


}
