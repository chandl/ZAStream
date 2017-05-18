package controllers;

import helper.HashHelper;
import helper.Secured;
import models.Channel;
import models.ChannelFactory;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.LegacyWebSocket;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.formdata.PrivateChannelForm;

import javax.inject.Inject;


/**
 * ChannelController: Controller to handle Channels & routing the proper stream.
 *
 * @author Chandler Severson <seversonc@sou.edu>
 * @author Yiwei Zheng <zhengy1@sou.edu>
 * @version 1.0
 * @since 1.0
 */
public class ChannelController extends Controller {

    @Inject
    FormFactory formFactory;
    Form<PrivateChannelForm> pcForm;

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

        // Channel is not public - redirect to password page.
        //      Don't redirect if they previously authenticated or are the channel owner.
        if(!Secured.isLoggedIn(ctx()) || !Secured.getUserInfo(ctx()).getUserName().equals(name)){
            if((!Secured.checkIfAuthenticatedToChannel(ctx(), c ) && !c.isPublic())) {
                pcForm = formFactory.form(PrivateChannelForm.class);
                return ok(views.html.private_channel.render(name, pcForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
            }
        }


        if(!Secured.checkIfViewedChannel(ctx(), c)){
            c.setTotalViews(c.getTotalViews() + 1);
            Secured.addViewedChannel(ctx(), c);
            c.save();
        }
        int totalViews = c.getTotalViews();
        String key = c.getStreamKey();

        return ok(views.html.channel.render(name, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), key, totalViews));
    }

    //Called when POSTing
    public Result showPrivate(String channel){

        User u = User.findByUsername(channel);
        Channel c = Channel.findChannel(u);

        //User already authenticated OR they are channel owner
        if(Secured.checkIfAuthenticatedToChannel(ctx(), c) || (Secured.isLoggedIn(ctx()) && Secured.getUserInfo(ctx()).getUserName().equals(channel))){
            return redirect(routes.ChannelController.show(channel));
        }


        DynamicForm dynform = Form.form().bindFromRequest();
        pcForm = formFactory.form(PrivateChannelForm.class);
        String pw = dynform.get("channelPassword");

        if(!checkChannelPassword(c, pw)){ // Bad Password
            pcForm.reject("badPassword" , "Invalid Channel Password Entered!");
            return badRequest(views.html.private_channel.render(channel, pcForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }else{
            Secured.addAuthenticatedChannel(ctx(), c);
            return redirect(routes.ChannelController.show(channel));
        }

    }

    public boolean checkChannelPassword(Channel channel, String pw){
        return HashHelper.checkPassword(pw, channel.getChannelPassword());
    }

    public Result webSocketChannel(String stream){
        return ok(views.js.channelWS.render(stream));
    }

    public LegacyWebSocket<String> viewCountInterface(String channel){
        return new LegacyWebSocket<String>() {
            @Override
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                ChannelWSController.start(in,out, Channel.findChannel(User.findByUsername(channel)));
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
