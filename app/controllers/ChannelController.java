package controllers;

import controllers.*;
import helper.Secured;
import models.Channel;
import models.ChannelFactory;
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
        Channel c = Channel.findChannel(u);

        if(!Secured.checkIfViewedChannel(ctx(), c)){
            c.setTotalViews(c.getTotalViews() + 1);
            Secured.addViewedChannel(ctx(), c);
            c.save();
        }
        int totalViews = c.getTotalViews();
        String key = c.getStreamKey();

        return ok(views.html.channel.render(name, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), key, totalViews));
    }

    public Result webSocketViewCount(String stream){
        return ok(views.js.viewCount.render(stream));
    }

    public LegacyWebSocket<String> viewCountInterface(String channel){
        return new LegacyWebSocket<String>() {
            @Override
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                ViewCountController.start(in,out, Channel.findChannel(User.findByUsername(channel)));
            }
        };
    }

    public Result changeStreamKey (String channelName){
        User user = Secured.getUserInfo(ctx());

        User owner =  User.findByUsername(channelName);

        if(user.equals(owner)) {
            Channel channel = Channel.findChannel(owner);

            String newKey = ChannelFactory.randomStreamKey();
            Logger.debug(String.format("The original stream key is : %s", channel.getStreamKey()));
            channel.setStreamKey(newKey);

            channel.save();
            Logger.debug(String.format("The stream key in the channel: %s is changed to new key : %s", channel.getOwner(), channel.getStreamKey()));
        }

        flash("streamKeyChanged", "streamKeyChanged");
        return redirect(controllers.routes.ChannelController.show(channelName));
    }



}
