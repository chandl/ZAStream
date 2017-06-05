package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ChatRoom: Model for Chat Rooms
 *
 * @author Chandler Severson
 * @author Yiwei Zheng
 * @version 2.0
 * @since 2.0
 */
@Entity
@Table(name="chatroom")
public class ChatRoom extends Model{

    public ChatRoom(Channel channel) {
        this.channel = channel;
        this.currentChatters = 0;
        this.chatCount = 0;
    }

    @Id
    @GeneratedValue
    @Column(name = "roomID")
    int roomId;

    @Column(name="currentChatters")
    int currentChatters;

    @Column(name="chatCount")
    int chatCount;

    @Column(name="publicRoom")
    boolean isPublic;

    @OneToMany(mappedBy = "chatRoom", targetEntity = Chat.class)
    List<Chat> chatMessages = new ArrayList<Chat>();

    @OneToOne(mappedBy = "chatRoom", targetEntity = Channel.class)
    Channel channel;

    public static Finder find = new Finder(Integer.class, ChatRoom.class);

    public static int findNumberOfPublicRooms(){
        List<ChatRoom> rooms = find.where().eq("isPublic", true).findList();
        return rooms.size();
    }

    public static ChatRoom findById(int id){
        List<ChatRoom> rooms = find.where().eq("roomId", id).findList();
        return(rooms.size() == 0)? null:rooms.get(0);
    }

    public Channel getChannel() {
        return channel;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public int getRoomId() {
        return roomId;
    }

    public void addChat(Chat chat){
        chatMessages.add(chat);
    }
}
