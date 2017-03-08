package controllers;

import helper.Secured;
import models.Channel;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;


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
        String key = c.getStreamKey();

        return ok(views.html.channel.render(name+"'s Channel", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), key));
    }

}
