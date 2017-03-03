package controllers;

import models.Channel;
import models.User;
import play.mvc.Result;
import views.html.*;

import static play.mvc.Http.Context.Implicit.ctx;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

/**
 * Created by chandler on 2/28/17.
 */
public class ChannelController {

//    public Result index(){
//        return ok(channel.render("Hello!", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
//    }

    public Result show(String name){

        if(!Secured.isUser(name)){
           return badRequest(views.html.fof.render("Bad Request - "+name, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }

        User u = User.findByUsername(name);
        Channel c= u.findChannel();
        String key = c.getStreamKey();

        return ok(views.html.channel.render(name+"'s Channel", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), key));
    }
}
