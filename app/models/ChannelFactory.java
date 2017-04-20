package models;
import java.util.UUID;

/**
 * Channel Factory - Create New {@link Channel}s w/ the Factory Design Pattern
 * @author Chandler Severson <seversonc@sou.edu>
 * @version 1.0
 * @since 1.0
 *
 */
public class ChannelFactory {

    /**
     * Creates a new Channel.
     *
     * @param channelType The Type of the {@link Channel}
     * @param owner The {@User} owner of the {@link Channel}
     * @return
     */
    public static Channel newChannel(String channelType, User owner) {
        Channel c = new Channel(channelType, randomStreamKey(), null, owner );
        ChatRoom room = new ChatRoom(c);
        room.save();
        c.setChatRoom(room);
        return c;
    }

    /**
     * Generate a random {@link UUID} stream key.
     *
     * @return a unique, random, 16-digit alphanumeric stream key.
     */
    private static String randomStreamKey(){
        String key;
        do{
            key = UUID.randomUUID().toString();
            key = key.replaceAll("-","");
            key = key.substring(0,16);
        }while(Channel.streamKeyExists(key)); //prevent duplicates

        return key;
    }
}
