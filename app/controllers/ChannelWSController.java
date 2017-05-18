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

public class ChannelWSController {
    private static Map<Integer, List<WebSocket.Out<String>>> connections = new HashMap<>();

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

    public static void updateChannelPassword(Channel c, String pw){
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

    public static void updateTitle(Channel c, String title){
        c.setChannelTitle(title);
        c.save();
        Logger.debug(String.format("[%s] set title to [%s]", c.getOwner().getUserName(), title));
        notifyAllTitleChange(c, title);
    }

    public static void increaseCount(Channel channel){
        channel.setCurrentViewers(connections.get(channel.getChannelID()).size());
        channel.save();
        notifyAllViewCount(channel, channel.getCurrentViewers());
        Logger.debug("New connection to " + channel.getOwner().getUserName() + "'s channel. Current Viewers: "+ channel.getCurrentViewers());
    }

    public static void decreaseCount(Channel channel, WebSocket.Out<String> conn){
        connections.get(channel.getChannelID()).remove(conn);
        channel.setCurrentViewers(connections.get(channel.getChannelID()).size());
        channel.save();
        notifyAllViewCount(channel, channel.getCurrentViewers());
        Logger.debug("Disconnection from " + channel.getOwner().getUserName() + "'s channel. Current Viewers: "+ channel.getCurrentViewers());
    }

    public static void notifyAllViewCount(Channel channel, int viewCount){
        for(WebSocket.Out<String> out : connections.get(channel.getChannelID())){
            out.write("["+viewCount+"]");
        }
    }

    public static void notifyAllTitleChange(Channel channel, String titleChange){
        for(WebSocket.Out<String> out : connections.get(channel.getChannelID())){
            out.write("[title]"+titleChange);
        }
    }

}
