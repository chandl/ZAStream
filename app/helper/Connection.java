package helper;

import models.User;
import play.mvc.WebSocket;

/**
 * Connection: Helper Class to keep track of {@link WebSocket} connections.
 *
 * @author Chandler Severson
 * @author Yiwei Zheng
 * @version 2.0
 * @since 2.0
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
