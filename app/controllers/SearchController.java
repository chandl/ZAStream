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

/**
 * SearchController: Controller to handle the search system.
 *
 * @author Chandler Severson
 * @author Yiwei Zheng
 * @version 2.0
 * @since 2.0
 */

public class SearchController extends Controller {

    /**
     * Controller method to show the Search Page based on a query.
     * @param query The search term to look up.
     *
     * @return {@code HTTP.ok} of the search.html with search results.
     */
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
