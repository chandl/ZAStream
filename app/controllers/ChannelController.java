package controllers;

import play.mvc.Result;
import views.html.*;

import static play.mvc.Http.Context.Implicit.ctx;
import static play.mvc.Results.ok;

/**
 * Created by chandler on 2/28/17.
 */
public class ChannelController {

    public Result index(){
        return ok(channel.render("Hello!", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    public Result show(String name){
        return ok(channel.render(name+"'s Channel", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }
}
