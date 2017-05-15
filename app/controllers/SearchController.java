package controllers;

import helper.Secured;
import models.Channel;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchController extends Controller {


    public Result searchPage(String query){
        Set<Channel> channels = new HashSet<Channel>();

        channels.addAll(Channel.findChannels(query));

        List<User> users = User.findUsers(query);

        for(User u : users){
            channels.add(u.getUserChannel());
        }

        User gc = User.findByUsername("gchat");
        if(gc != null){
            Channel gchat = gc.getUserChannel();
            channels.remove(gchat);
        }
        return ok(views.html.search.render(query, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), new ArrayList(channels)));
    }

}
