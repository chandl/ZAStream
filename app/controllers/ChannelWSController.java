package controllers;

import helper.Secured;
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
                    Logger.info("Bad Input ChannelWSController : " + s);
                    return ;
                }

                int userId = Integer.parseInt(info[0]);
                User user = User.findById(userId);
                Channel channel = Channel.findChannel(user);
                channel.setChannelTitle(info[1]);

                channel.save();

                notifyAllTitleChange(channel, info[1]);
            }
        });
        in.onClose(() -> ChannelWSController.decreaseCount(channel, out));
    }

    public static void increaseCount(Channel channel){
        channel.setCurrentViewers(connections.get(channel.getChannelID()).size());
        channel.save();
        notifyAllViewCount(channel, channel.getCurrentViewers());
        Logger.debug("Someone Connected to " + channel.getOwner().getUserName() + "'s channel. Current Viewers: "+ channel.getCurrentViewers());
    }

    public static void decreaseCount(Channel channel, WebSocket.Out<String> conn){
        connections.get(channel.getChannelID()).remove(conn);
        channel.setCurrentViewers(connections.get(channel.getChannelID()).size());
        channel.save();
        notifyAllViewCount(channel, channel.getCurrentViewers());
        Logger.debug("Someone Disconnected from " + channel.getOwner().getUserName() + "'s channel. Current Viewers: "+ channel.getCurrentViewers());
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
