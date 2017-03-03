package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name="channel")
public class Channel extends Model{
    @Id
    @GeneratedValue
    @Column(name="channelID")
    public int channelID;

    @Column(name="currentViewers")
    public int currentViewers;

    @Column(name="totalViews")
    public int totalViews;

    @Column(name="channelType")
    @Size(max=3, min=3)
    public String channelType;

    @Column(name="streamKey")
    @Size(min=16, max=16)
    public String streamKey;

    @OneToOne
    @JoinColumn(name="roomID")
    public ChatRoom chatRoom;

    @OneToOne
    @JoinColumn(name="userID")
    public User owner;


    public static Finder find = new Finder(Integer.class, Channel.class);

}
