package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name="channel")
public class Channel extends Model{
    @Id
    @GeneratedValue
    @Column(name="channelID")
    public int channelID;

    @Column(name="currentViewers")
    public int currentViewers = 0;

    @Column(name="totalViews")
    public int totalViews = 0;

    @Column(name="channelType")
    @Size(max=3, min=3)
    public String channelType = "PUB";

    @Column(name="streamKey")
    @Size(min=16, max=16)
    public String streamKey;

    @OneToOne
    @JoinColumn(name="roomID")
    public ChatRoom chatRoom;

    @OneToOne
    @JoinColumn(name="userID")
    public User owner;


    public Channel(String channelType, String streamKey, ChatRoom chatRoom, User owner) {
        this.channelType = channelType;
        this.streamKey = streamKey;
        this.chatRoom = chatRoom;
        this.owner = owner;
        this.currentViewers = 0;
        this.totalViews = 0;
    }

    public static Finder find = new Finder(Integer.class, Channel.class);

    public static boolean streamKeyExists(String key){
        List<Channel> channels = find.where().eq("streamKey", key).findList();

        return channels.size() > 0;
    }

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public int getCurrentViewers() {
        return currentViewers;
    }

    public void setCurrentViewers(int currentViewers) {
        this.currentViewers = currentViewers;
    }

    public int getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(int totalViews) {
        this.totalViews = totalViews;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getStreamKey() {
        return streamKey;
    }

    public void setStreamKey(String streamKey) {
        this.streamKey = streamKey;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public static Finder getFind() {
        return find;
    }

    public static void setFind(Finder find) {
        Channel.find = find;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Channel: ").append(channelID).append("\n  Viewers: ").append(currentViewers).append("\n  Type: ").append(channelType).append("\n  Key: ").append(streamKey).append("\n  Owner: ").append(owner.toString());

        return sb.toString();
    }
}
