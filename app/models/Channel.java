package models;

import com.avaje.ebean.Model;
import play.Logger;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.File;
import java.util.List;

/**
 * Channel: Data Model + Helper Methods for Channels
 *
 * @author Chandler Severson <seversonc@sou.edu>
 * @author Yiwei Zheng <zhengy1@sou.edu>
 * @version 1.0
 * @since 1.0
 */
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

    @Column(name="channelTitle")
    @Size(min=3, max=64)
    public String channelTitle;

    @OneToOne
    @JoinColumn(name="roomID")
    public ChatRoom chatRoom;

    @OneToOne
    @JoinColumn(name="userID")
    public User owner;

    /**
     * Create a new Channel
     *
     * @param channelType The Type of the {@link Channel} (PUB or PRI)
     * @param streamKey The Stream key to be associated with the {@link Channel}.
     * @param chatRoom The {@link ChatRoom} to be associated with the {@link Channel}
     * @param owner The {@link User} that owns this channel.
     */
    public Channel(String channelType, String streamKey, ChatRoom chatRoom, User owner) {
        this.channelType = channelType;
        this.streamKey = streamKey;
        this.chatRoom = chatRoom;
        this.owner = owner;
        this.currentViewers = 0;
        this.totalViews = 0;
        this.channelTitle = owner.getUserName();
    }

    /**
     * The {@link com.avaje.ebean.Model.Finder} method to find entries in the DB.
     */
    public static Finder find = new Finder(Integer.class, Channel.class);

    /**
     * Checks if a specified Stream key exists.
     * @param key The stream key to check.
     * @return true if the stream key exists, otherwise return false
     */
    public static boolean streamKeyExists(String key){
        List<Channel> channels = find.where().eq("streamKey", key).findList();

        return channels.size() > 0;
    }

    public static boolean isStreaming(String key){
        File streamFile = new File("/HLS/live/"+key+"/index.m3u8");

        return streamFile.exists();
    }


    //====================Getters and Setters====================
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
        if(currentViewers >= 0)
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
    //====================END Getters and Setters====================

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Channel: ")
                .append(channelID)
                .append("\n  Viewers: ")
                .append(currentViewers)
                .append("\n  Type: ")
                .append(channelType)
                .append("\n  Key: ")
                .append(streamKey)
                .append("\n  Owner: ")
                .append(owner.toString());

        return sb.toString();
    }

    public static Channel findChannel(User user){
        List<Channel> theChannel = find.where().eq("userID", user.userId).findList();

        if(theChannel.size() == 0) {
//            Logger.debug("No Channel Found for ID: "+ user.userId + ", Name: "+ user.userName);
            return null;
        }else{
//            Logger.debug("Channel Found for ID: "+ user.userId + ", Name: "+ user.userName+", Key: "+theChannel.get(0).getStreamKey());
            return theChannel.get(0);
        }
    }

    public static List<Channel> findChannels(String query){
        List<Channel> channels = find.where().like("channelTitle", "%"+query+"%").findList();

        return channels;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }
}
