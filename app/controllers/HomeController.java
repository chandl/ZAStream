package controllers;

import helper.Secured;
import models.Channel;
import play.mvc.*;
import views.html.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * HomeController: Controller to handle the Homepage.
 *
 * @author Chandler Severson
 * @author Yiwei Zheng
 * @version 2.0
 * @since 1.0
 */
public class HomeController extends Controller {


    /**
     * Controller method to display the HomePage and Featured Streams.
     *
     * @return <code>HTTP OK</code> result, rendering the Homepage.
     */
    public Result index() {
        ArrayList<Channel> channels = ChannelController.findLiveChannels();

        if(channels.size() < 6){
            ArrayList<Channel> nonLiveChannels = ChannelController.findNonLiveChannels();

            for(int i = channels.size(),  b =0; i < 6; i++ ){
                channels.add(nonLiveChannels.get(b++));
            }

        }

        return ok(index.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), channels));
    }

}