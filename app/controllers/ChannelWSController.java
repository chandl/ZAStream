package controllers;

import helper.HashHelper;
import models.Channel;
import models.User;
import play.Logger;
import play.mvc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
/**
 * ChannelWSController: Controller to handle the Channel WebSocket connections.
 *
 * @author Chandler Severson
 * @author Yiwei Zheng
 * @version 2.0
 * @since 2.0
 */
public class ChannelWSController {
    private static Map<Integer, List<WebSocket.Out<String>>> connections = new HashMap<>();

    /**
     * Method to control WebSocket connections to channels.
     * 
     * @param in The InputStream of the Websocket.
     * @param out The OutputStream of the Websocket.
     *  @param channel The {@link Channel} that the Websocket is on. 
     */
    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out, Channel channel){
        List<WebSocket.Out<String>> conn;
        if((conn = connections.get(channel.getChannelID()) ) !=  null){
//            Logger.debug("New Connection connections not null" );
            conn.add(out);
        }else{
//            Logger.debug("New Connection! connections null");
            conn = new ArrayList<>();
            conn.add(out);
            connections.put(channel.getChannelID(), conn);
        }

        increaseCount(channel);
        in.onMessage(new Consumer<String>() {
            @Override
            public void accept(String s) {
                String[] info = s.split("\n");
                if(info.length < 1){
                    Logger.error("Bad Input ChannelWSController : " + s);
                    return ;
                }

                //Contains user ID and message type
                String[] msgType = info[0].split(",");
                if(msgType.length < 1){
                    Logger.error("Bad Input ChannelWSController : " + s);
                    return ;
                }

                int userId = Integer.parseInt(msgType[0]);
                User user = User.findById(userId);
                Channel channel = Channel.findChannel(user);

                switch(msgType[1]){
                       case "T":
                           updateTitle(channel, info[1]);
                           break;
                       case "P":
                           updateChannelPassword(channel, info[1]);
                        break;
                }
            }
        });
        in.onClose(() -> ChannelWSController.decreaseCount(channel, out));
    }

    /**
     * Method that set the type of channel (Public/Private).
     * Set the Channel password and save to the database if the channel type is private.
     * 
     * @param c The channel to change the type.
     * @param pw The password of the private channel.
     */

    private static void updateChannelPassword(Channel c, String pw){
        if(pw == null || pw.equals(" ")){
            c.setChannelPassword(null);
            c.setChannelType("PUB");
            c.save();
            Logger.debug(String.format("[%s] is now [PUB]", c.getOwner().getUserName()));
        }else{
            String hashedPw = HashHelper.createPassword(pw);
            c.setChannelPassword(hashedPw);
            c.setChannelType("PRI");
            c.save();
            Logger.debug(String.format("[%s] is now [PRI]", c.getOwner().getUserName()));
        }
    }

    /**
     * Method to set and save the title of the {@link Channel}.
     * 
     * @param c The channel that set the title.
     * @param title The title that set to.
     */
    private static void updateTitle(Channel c, String title){
        c.setChannelTitle(title);
        c.save();
        Logger.debug(String.format("[%s] set title to [%s]", c.getOwner().getUserName(), title));
        notifyAllTitleChange(c, title);
    }


    /**
     * Method to increase the View Count of a channel. 
     * 
     * @param channel The channel that the view count should be increased on.
     */
    private static void increaseCount(Channel channel){
        channel.setCurrentViewers(connections.get(channel.getChannelID()).size());
        channel.save();
        notifyAllViewCount(channel, channel.getCurrentViewers());
        Logger.debug("New connection to " + channel.getOwner().getUserName() + "'s channel. Current Viewers: "+ channel.getCurrentViewers());
    }

    /**
     *  Method to decrease the View Count of a channel
     *  
     * @param channel The channel that the view count should be decreased on.
     * @param conn The connection that is disconnected.
     */

    private static void decreaseCount(Channel channel, WebSocket.Out<String> conn){
        connections.get(channel.getChannelID()).remove(conn);
        channel.setCurrentViewers(connections.get(channel.getChannelID()).size());
        channel.save();
        notifyAllViewCount(channel, channel.getCurrentViewers());
        Logger.debug("Disconnection from " + channel.getOwner().getUserName() + "'s channel. Current Viewers: "+ channel.getCurrentViewers());
    }

    /**
     * Method to notify all users in a {@link Channel} of an update in a View Count.
     *
     * Sends messages to the clients like '[i]', where i is the number of current viewers.
     *
     * @param channel The Channel that had a view count change.
     * @param viewCount The view count that displays.
     */
    private static void notifyAllViewCount(Channel channel, int viewCount){
        for(WebSocket.Out<String> out : connections.get(channel.getChannelID())){
            out.write("["+viewCount+"]");
        }
    }

    /**
     * Method to notify all users in a {@link Channel} of an update in the Channel's title.
     *
     * Sends messages to the clients like '[title]New Title'
     *
     * @param channel The Channel that had a Title change.
     * @param titleChange The new Title for the channel.
     */
    private static void notifyAllTitleChange(Channel channel, String titleChange){
        for(WebSocket.Out<String> out : connections.get(channel.getChannelID())){
            out.write("[title]"+titleChange);
        }
    }

}
