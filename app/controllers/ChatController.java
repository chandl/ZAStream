package controllers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import helper.Connection;
import helper.Secured;
import models.*;
import play.Logger;
import play.mvc.*;
import views.html.gchat;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class ChatController extends Controller {
    private static Map<Integer, HashSet<Connection<String>>> connections = new HashMap<>();

    public Result generalChat(){
        return ok(gchat.render("General Chat Room", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    public LegacyWebSocket<String> gchatInterface(int chatRoom){
        User requestingUser = Secured.getUserInfo(ctx());

        return new LegacyWebSocket<String>() {
            @Override
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                ChatController.start(in,out, ChatRoom.findById(chatRoom), requestingUser);
            }
        };

    }

    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out, ChatRoom room, User user){
        HashSet<Connection<String>> connList;

        Connection<String> newConnection = new Connection<>(in,out,user);

//        Logger.info("New User Connected to Chat: "+ channel.toString());
        if((connList = connections.get(room.getRoomId()) ) !=  null){

            //Check if user is already connected
            boolean userAlreadyConnected = false;
            for(Connection<String> conn : connList){
                if(conn.getConnectedUser().getUserName().equals(user.getUserName())){
                    userAlreadyConnected = true;
                    break;
                }
            }
            if(!userAlreadyConnected){
                //Update everyone's user list:
                sendUserListUpdate(room, true, newConnection.getConnectedUser().getUserName());
            }

            connList.add(newConnection);

        }else{
            connList = new HashSet<>();
            connList.add(newConnection);
            connections.put(room.getRoomId(), connList);
        }



        //Update the new user's list with every connection:
        sendUserList(room, newConnection);

        //Welcome message
        out.write("{\"sender\":\"ZAStream\", \"message\":\"Hello "+ user.getUserName() +", Welcome to the Chat!\"}");

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

                room.addChat(chat);

                String senderUserName = sender.userName;

                //Send the message to everyone.
                ChatController.notifyAll(room, msg, senderUserName);

            }
        });

        in.onClose(() -> close(room, newConnection));
    }

    /**
     * Sends the user list for the room to the {@link Connection}
     * @param room The {@link ChatRoom} to get the user list from.
     * @param connection The {@link Connection} to send the user list to.
     */
    private static void sendUserList(ChatRoom room, Connection<String> connection){
        StringBuilder sb = new StringBuilder();

        HashSet<Connection<String>> cs;

        Set<String> userList = new HashSet<>();

        if( (cs = connections.get(room.getRoomId())) != null){
            Iterator it = cs.iterator();
            while(it.hasNext() ){
                Connection<String> conn = (Connection<String>)it.next();

                String userName = conn.getConnectedUser().getUserName();
                if(!userList.contains(userName)){
                    sb.append(userName);

                    if(it.hasNext()){
                        sb.append("||");
                    }
                    userList.add(userName);
                }
            }
        }

        String out = sb.toString();

        notify(connection, out, "?chatUpdate");
    }

    /**
     * Sends a user list update to all of the users connected to a specified {@link ChatRoom}
     * @param room The {@link ChatRoom} to send the update to.
     * @param addUser {@code True} if the update is to ADD a user, {@code False} if the update is to REMOVE a user.
     * @param user The User Name of the {@link User} that joined/left the channel.
     */
    private static void sendUserListUpdate(ChatRoom room, boolean addUser, String user){

        if(addUser){
            notifyAll(room, user, "?chatJoin");
        }else{

            int count = 0;
            for(Connection<String> conn : connections.get(room.getRoomId())){
                if(conn.getConnectedUser().getUserName().equals(user)) count ++;
            }

            //If there are multiple of the same user in the chat, don't send the chatLeave message
            if(count == 1) notifyAll(room, user, "?chatLeave");
        }
    }

    public static void close(ChatRoom room, Connection<String> conn){
        sendUserListUpdate(room, false, conn.getConnectedUser().getUserName());
        connections.get(room.getRoomId()).remove(conn);
//        Logger.info("User Disconnected from Chat: "+ channel);
    }

    public static void notifyAll(ChatRoom room, String message, String sender){
        StringBuilder sb = new StringBuilder();
        sb.append("{\"sender\":\"").append(sender).append("\", \"message\":\"").append(message).append("\"}");

        String output = sb.toString();
        for(Connection<String> conn : connections.get(room.getRoomId()) ){
            conn.getOutputStream().write(output);
        }
    }

    private static void notify(Connection<String> connection, String message, String sender){
        StringBuilder sb = new StringBuilder();
        sb.append("{\"sender\":\"").append(sender).append("\", \"message\":\"").append(message).append("\"}");

        connection.getOutputStream().write(sb.toString());
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
        User requestingUser = Secured.getUserInfo(ctx());

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
                ChatController.start(in,out,room, requestingUser);
            }
        };

    }

    public Result webSocketGeneralChat(int userId){

        //Return bad request if user's cookies don't match the user ID passed in.
        if( !Secured.getUserInfo(ctx()).equals(User.findById(userId))){
            return badRequest();
        }

        return ok(views.js.chat.render("gchat", userId));
    }

    public Result webSocketChannelChat(String stream, int userId){

        //Return bad request if user's cookies don't match the user ID passed in.
        if( !Secured.getUserInfo(ctx()).equals(User.findById(userId)) ){
            return badRequest();
        }

        return ok(views.js.chat.render(stream, userId));
    }
}
