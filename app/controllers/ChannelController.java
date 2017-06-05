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
import java.util.ArrayList;
import java.util.List;


/**
 * ChannelController: Controller to handle Channels & routing the proper stream.
 *
 * @author Chandler Severson
 * @author Yiwei Zheng
 * @version 2.0
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

    /**
     * Controller method to show a private channel page.
     *
     * Checks if user is authenticated to a channel & checks the entered password.
     *
     * @param channel The Username of the owner of the {@link Channel} that the user is connecting to.
     * @return {@code badRequest} if the invalid password is entered, {@code redirect} to the channel page if the password is correct.
     */
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

        if(!HashHelper.checkPassword(pw, c.getChannelPassword())){ // Bad Password
            pcForm.reject("badPassword" , "Invalid Channel Password Entered!");
            return badRequest(views.html.private_channel.render(channel, pcForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }else{
            Secured.addAuthenticatedChannel(ctx(), c);
            return redirect(routes.ChannelController.show(channel));
        }

    }

    /**
     * Reverse Javascript Route to get the channelWS.js rendered properly.
     *
     * @param stream The Username of the owner of a {@link Channel}
     * @return
     */
    public Result webSocketChannel(String stream){
        return ok(views.js.channelWS.render(stream));
    }

    /**
     * Controller to create a Channel  WebSocket connection: {@link ChannelWSController}.
     *
     * @param channel The Username of the owner of a {@link Channel} to start the viewCount WS Interface.
     * @return The ViewCount WebSocket interface for the specified channel.
     */
    public LegacyWebSocket<String> viewCountInterface(String channel){
        return new LegacyWebSocket<String>() {
            @Override
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                ChannelWSController.start(in,out, Channel.findChannel(User.findByUsername(channel)));
            }
        };
    }

    /**
     * Controller method to change the stream key.
     *
     * Called when {@code POST}ing to {@code /reset-streamkey/:channel}.
     *
     * @param channelName The name of the channel to reset the stream key for.
     * @return {@code HTTP.redirect} back to the channel page.
     */
    public Result changeStreamKey (String channelName){
        User user = Secured.getUserInfo(ctx());

        User owner =  User.findByUsername(channelName);

        //Make sure the requester is the owner of the channel :
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

    /**
     * Helper method to find all currently Live/Streaming channels.
     *
     * @return An ArrayList of the top {@link Channel}s that are currently streaming.
     */
    static ArrayList<Channel> findLiveChannels(){
        List<Channel> c = Channel.find.where()
                .eq("channelType", "PUB").and()
                .eq("channelStatus", true)
                .orderBy().desc("currentViewers")
                .orderBy().desc("totalViews")
                .findList();


        ArrayList<Channel> channels = new ArrayList<>();
        for(int i=0; i<6 && i<c.size(); i++){
            channels.add(c.get(i));
        }

        channels.trimToSize();

        return channels;
    }

    /**
     * Helper method to find all non-active (not streaming) channels.
     *
     * @return An ArrayList of the top {@link Channel}s that are not currently streaming.
     */
    static ArrayList<Channel> findNonLiveChannels(){
        List<Channel> c = Channel.find.where()
                .eq("channelType", "PUB").and()
                .eq("channelStatus", false)
                .orderBy().desc("totalViews")
                .orderBy().desc("currentViewers").findList();

        ArrayList<Channel> channels = new ArrayList<>();
        for(int i=0; i<6 && i<c.size(); i++){
            channels.add(c.get(i));
        }

        return channels;
    }

}
