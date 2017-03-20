package models;

import com.avaje.ebean.Model;
import play.data.format.Formats;
import javax.persistence.*;
import java.sql.Date;

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

    @Column(name="sentTime")
    @Formats.DateTime(pattern="yyyy-mm-dd")
    public Date sentTime;

    public static Finder<Integer, Chat> find = new Finder<Integer, Chat>(Chat.class);

}
