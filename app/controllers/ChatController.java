package controllers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Channel;
import models.Chat;
import models.User;
import play.Logger;
import play.mvc.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class ChatController {
    private static Map<Integer, List<WebSocket.Out<String>>> connections = new HashMap<>();

    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out, Channel channel){
        List<WebSocket.Out<String>> conn;
//        Logger.info("New User Connected to Chat: "+ channel.toString());
        if((conn = connections.get(channel.getChannelID()) ) !=  null){
            conn.add(out);
        }else{
            conn = new ArrayList<>();
            conn.add(out);
            connections.put(channel.getChannelID(), conn);
        }

        out.write("{\"sender\":\"ZAStream\", \"message\":\"Hello, Welcome to the Chat!\"}");

        in.onMessage(new Consumer<String>() {
            @Override
            public void accept(String message) {
                Logger.info("New Message: "+ message);

                //Parse the JSON message
                ObjectMapper mapper = new ObjectMapper();
                JsonNode obj = null;
                try {
                    obj = mapper.readTree(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int sendingID = obj.get("sender").asInt();
                String msg = obj.get("message").asText();
                User sender = User.findById(sendingID);
                //Save the message to the DB.
                Chat chat = new Chat(sender, null, msg);
                chat.save();

                String senderUserName = sender.userName;

                //Send the message to everyone.
                ChatController.notifyAll(channel, msg, senderUserName);
            }
        });

        in.onClose(() -> close(channel, out));
    }

    public static void close(Channel channel, WebSocket.Out<String> conn){
        connections.get(channel.getChannelID()).remove(conn);
//        Logger.info("User Disconnected from Chat: "+ channel);
    }

    public static void notifyAll(Channel channel, String message, String sender){
        StringBuilder sb = new StringBuilder();
        sb.append("{\"sender\":\"").append(sender).append("\", \"message\":\"").append(message).append("\"}");

        String output = sb.toString();
        for(WebSocket.Out<String> out : connections.get(channel.getChannelID())){
            out.write(output);
        }
    }



}
