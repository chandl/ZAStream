package helper;

import models.User;
import play.mvc.WebSocket;

/**
 * Created by chandler on 5/28/17.
 */
public class Connection<T> {

    private WebSocket.In<T> in;
    private WebSocket.Out<T> out;
    private User connectedUser;

    public Connection(WebSocket.In<T> in, WebSocket.Out<T> out, User connectedUser){
        this.in = in;
        this.out = out;
        this.connectedUser = connectedUser;
    }

    public WebSocket.In<T> getInputStream() {
        return in;
    }

    public WebSocket.Out<T> getOutputStream() {
        return out;
    }

    public User getConnectedUser() {
        return connectedUser;
    }

}
