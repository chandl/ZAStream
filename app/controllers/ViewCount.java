package controllers;

import models.Channel;
import play.Logger;
import play.mvc.*;

import java.util.ArrayList;
import java.util.List;

public class ViewCount {
    private static List<WebSocket.Out<String>> connections = new ArrayList<>();

    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out, Channel channel){
        connections.add(out);

        increaseCount(channel);

        in.onClose(() -> ViewCount.decreaseCount(channel, out));
    }


    public static void increaseCount(Channel channel){
        channel.setCurrentViewers(channel.getCurrentViewers() + 1);
        channel.save();
        notifyAll(channel.getCurrentViewers());
        Logger.info("Someone Connected to " + channel.getOwner().getUserName() + "'s channel. Current Viewers: "+ channel.getCurrentViewers());
    }

    public static void decreaseCount(Channel channel, WebSocket.Out<String> conn){
        channel.setCurrentViewers(channel.getCurrentViewers() - 1);
        channel.save();
        connections.remove(conn);
        notifyAll(channel.getCurrentViewers());
        Logger.info("Someone Disconnected from " + channel.getOwner().getUserName() + "'s channel. Current Viewers: "+ channel.getCurrentViewers());
    }

    public static void notifyAll(int viewCount){
        for(WebSocket.Out<String> out : connections){
            out.write(""+viewCount);
        }
    }

}
