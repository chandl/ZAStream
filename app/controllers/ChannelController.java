package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import helper.Secured;
import models.Channel;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.LegacyWebSocket;
import play.mvc.Result;
import play.mvc.WebSocket;


/**
 * ChannelController: Controller to handle Channels & routing the proper stream.
 *
 * @author Chandler Severson <seversonc@sou.edu>
 * @author Yiwei Zheng <zhengy1@sou.edu>
 * @version 1.0
 * @since 1.0
 */
public class ChannelController extends Controller {

    /**
     * Controller method to show a channel page.
     *
     * @param name The channel to show.
     * @return <code>badRequest</code> if channel is not a user, <code>OK</code> if user exists.
     */
    public Result show(String name){

        if(!User.isUser(name)){
           return badRequest(views.html.fof.render("Bad Request - "+name, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }

        User u = User.findByUsername(name);
        Channel c= Channel.findChannel(u);

        c.setTotalViews(c.getTotalViews() + 1);
        c.save();
        int totalViews = c.getTotalViews();
        String key = c.getStreamKey();

        return ok(views.html.channel.render(name, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), key, totalViews));
    }

    public Result webSocketViewCount(String stream){
        return ok(views.js.viewCount.render(stream));
    }

    public Result webSocketChat(String stream){
        return ok(views.js.chat.render(stream));
    }

    public LegacyWebSocket<String> viewCountInterface(String channel){

        Logger.info("viewCountInterface Called. Channel: "+channel);
        return new LegacyWebSocket<String>() {
            @Override
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                ViewCountController.start(in,out, Channel.findChannel(User.findByUsername(channel)));
            }
        };
    }

    public LegacyWebSocket<JsonNode> chatInterface(String channel){
        return new LegacyWebSocket<JsonNode>() {
            @Override
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
                ChatController.start(in,out, Channel.findChannel(User.findByUsername(channel)));
            }
        };

    }

}
