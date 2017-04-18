package controllers;

import models.Channel;
import play.Logger;
import play.mvc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewCountController {
    private static Map<Integer, List<WebSocket.Out<String>>> connections = new HashMap<>();

    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out, Channel channel){
        List<WebSocket.Out<String>> conn;
        if((conn = connections.get(channel.getChannelID()) ) !=  null){
            Logger.debug("New Connection! connections not null");
            conn.add(out);
        }else{
            Logger.debug("New Connection! connections null");
            conn = new ArrayList<>();
            conn.add(out);
            connections.put(channel.getChannelID(), conn);
        }

        increaseCount(channel);

        in.onClose(() -> ViewCountController.decreaseCount(channel, out));
    }

    public static void increaseCount(Channel channel){
        channel.setCurrentViewers(connections.get(channel.getChannelID()).size());
        channel.save();
        notifyAll(channel, channel.getCurrentViewers());
        Logger.info("Someone Connected to " + channel.getOwner().getUserName() + "'s channel. Current Viewers: "+ channel.getCurrentViewers());
    }

    public static void decreaseCount(Channel channel, WebSocket.Out<String> conn){
        connections.get(channel.getChannelID()).remove(conn);
        channel.setCurrentViewers(connections.get(channel.getChannelID()).size());
        channel.save();
        notifyAll(channel, channel.getCurrentViewers());
        Logger.info("Someone Disconnected from " + channel.getOwner().getUserName() + "'s channel. Current Viewers: "+ channel.getCurrentViewers());
    }

    public static void notifyAll(Channel channel, int viewCount){
        for(WebSocket.Out<String> out : connections.get(channel.getChannelID())){
            out.write(""+viewCount);
        }
    }

}
