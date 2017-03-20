package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="chatRoom")
public class ChatRoom extends Model{

    @Id
    @GeneratedValue
    @Column(name = "roomID")
    int roomId;

    @Column(name="currentChatters")
    int currentChatters;

    @Column(name="chatCount")
    int chatCount;

    @OneToMany(mappedBy = "chatRoom", targetEntity = Chat.class)
    List<Chat> chatMessages = new ArrayList<Chat>();

    @OneToOne(mappedBy = "chatRoom", targetEntity = Channel.class)
    Channel channel;

    public static Finder find = new Finder(Integer.class, ChatRoom.class);
}
