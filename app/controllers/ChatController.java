package controllers;
import com.fasterxml.jackson.databind.JsonNode;
import models.Channel;
import play.Logger;
import play.mvc.*;
import java.util.*;
import java.util.function.Consumer;

public class ChatController {
    private static Map<Integer, List<WebSocket.Out<JsonNode>>> connections = new HashMap<>();

    public static void start(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out, Channel channel){
        List<WebSocket.Out<JsonNode>> conn;
        Logger.info("New User Connected to Chat: "+ channel.toString());
        if((conn = connections.get(channel.getChannelID()) ) !=  null){
            conn.add(out);
        }else{
            conn = new ArrayList<>();
            conn.add(out);
            connections.put(channel.getChannelID(), conn);
        }

        in.onMessage(new Consumer<JsonNode>() {
            @Override
            public void accept(JsonNode message) {
                Logger.info("New Message: "+ message);
                ChatController.notifyAll(channel, message);
            }
        });

        in.onClose(() -> close(channel, out));
    }

    public static void close(Channel channel, WebSocket.Out<JsonNode> conn){
        connections.get(channel.getChannelID()).remove(conn);
        Logger.info("User Disconnected from Chat: "+ channel);
    }

    public static void notifyAll(Channel channel, JsonNode message){
        for(WebSocket.Out<JsonNode> out : connections.get(channel.getChannelID())){
//            out.write(""+viewCount);
        }
    }



}
