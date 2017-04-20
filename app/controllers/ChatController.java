package controllers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import helper.Secured;
import models.*;
import play.Logger;
import play.mvc.*;
import views.html.gchat;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class ChatController extends Controller {
    private static Map<Integer, List<WebSocket.Out<String>>> connections = new HashMap<>();

    public Result generalChat(){
        return ok(gchat.render("General Chat Room", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    public LegacyWebSocket<String> gchatInterface(int chatRoom){
        return new LegacyWebSocket<String>() {
            @Override
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                ChatController.start(in,out, ChatRoom.findById(chatRoom));
            }
        };

    }

    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out, ChatRoom room){
        List<WebSocket.Out<String>> conn;
//        Logger.info("New User Connected to Chat: "+ channel.toString());
        if((conn = connections.get(room.getRoomId()) ) !=  null){
            conn.add(out);
        }else{
            conn = new ArrayList<>();
            conn.add(out);
            connections.put(room.getRoomId(), conn);
        }

        out.write("{\"sender\":\"ZAStream\", \"message\":\"Hello, Welcome to the Chat!\"}");

        in.onMessage(new Consumer<String>() {
            @Override
            public void accept(String message) {
                Logger.debug(String.format("Chat[room: %d] %s", room.getRoomId(), message));

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
                Chat chat = new Chat(sender, room, msg);
                chat.save();

                String senderUserName = sender.userName;

                //Send the message to everyone.
                ChatController.notifyAll(room, msg, senderUserName);
            }
        });

        in.onClose(() -> close(room, out));
    }

    public static void close(ChatRoom room, WebSocket.Out<String> conn){
        connections.get(room.getRoomId()).remove(conn);
//        Logger.info("User Disconnected from Chat: "+ channel);
    }

    public static void notifyAll(ChatRoom room, String message, String sender){
        StringBuilder sb = new StringBuilder();
        sb.append("{\"sender\":\"").append(sender).append("\", \"message\":\"").append(message).append("\"}");

        String output = sb.toString();
        for(WebSocket.Out<String> out : connections.get(room.getRoomId())){
            out.write(output);
        }
    }

    public boolean doesGChatExist(){
        if(ChatRoom.findNumberOfPublicRooms() >= 1) return true;
        else return false;
    }

    private Channel createGeneralChat(){
        //Create user
        String uuid = UUID.randomUUID().toString();
        User user = new User("gchat", uuid.replaceAll("-",""), "gchat@zastream.com");
        user.save();

        //Create channel
        Channel c = ChannelFactory.newChannel("PUB", user);
        c.save();

        //Create chatroom
        ChatRoom room = c.getChatRoom();
        room.setPublic(true);
        room.save();

        return c;
    }

    public LegacyWebSocket<String> chatInterface(String channel){
        Channel c = null;
        if(channel.equals("gchat")){//general chat room
            if(!doesGChatExist()){
                 c = createGeneralChat();
            }else{
                c = Channel.findChannel(User.findByUsername("gchat"));
            }
        }else{
            c = Channel.findChannel(User.findByUsername(channel));
        }

        ChatRoom room = c.getChatRoom();

        return new LegacyWebSocket<String>() {
            @Override
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                ChatController.start(in,out,room);
            }
        };

    }

    public Result webSocketGeneralChat(int userId){
        return ok(views.js.chat.render("gchat", userId));
    }

    public Result webSocketChannelChat(String stream, int userId){
        return ok(views.js.chat.render(stream, userId));
    }
}
