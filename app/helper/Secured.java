package helper;

import models.Channel;
import models.User;
import play.api.libs.Crypto;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import static play.mvc.Controller.request;
import static play.mvc.Controller.response;
import static play.mvc.Controller.session;

/**
 * Secured: Class to handle Authentication and Session Storage
 *
 * @author Chandler Severson
 * @author Yiwei Zheng
 * @version 2.0
 * @since 1.0
 */
public class Secured extends Security.Authenticator {

    /**
     * Get the username of the user using the site.
     *
     * @param ctx the {@link play.mvc.Http.Context} of the user's session.
     * @return The username of the authenticated user.
     */
    @Override
    public String getUsername(Http.Context ctx) {
        return ctx.session().get("userName");
    }

    /**
     * Redirects the user to the HomePage if they are doing stuff that requires authentication.
     *
     * @param ctx the {@link play.mvc.Http.Context} of the user's session.
     * @return <code>redirect</code> the user to the Homepage.
     */
    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return redirect(controllers.routes.HomeController.index());
    }

    /**
     * Gets the userName of the currently authenticated user.
     * @param ctx the {@link play.mvc.Http.Context} of the user's session.
     * @return The userName of the authenticated user.
     */
    public static String getName(Http.Context ctx){
        return ctx.session().get("userName");
    }

    /**
     * Method to see if user is currently logged in.
     *
     * @param ctx the {@link play.mvc.Http.Context} of the user's session.
     * @return true if the user is authenticated, false otherwise
     */
    public static boolean isLoggedIn(Http.Context ctx){
        return (getName(ctx) != null);
    }

    /**
     * Method that authenticate by storing the user's userName in their cookies. 
     * 
     * @param ctx The HTTP context (User's cookies). 
     * @param userName The userName of the {@link User} that is being authenticate.
     */
    public static void authenticateUser(Http.Context ctx, String userName){
        ctx.session().clear();
        ctx.session().put("userName", userName);
        session("userName", userName);
    }

    /**
     * Method that adds a cookie to the user's session that verifies that
     * the user has viewed the channel. The cookie expires after 24 hours.
     * 
     * This makes sure that the user cannot keep refreshing the page to increase the total view count. 
     * Used with {@code checkIfViewedChannel}
     * 
     * @param ctx The Http.Context of the user.
     * @param channel The {@link Channel} that the user viewed.  
     */
    public static void addViewedChannel(Http.Context ctx, Channel channel){
        response().setCookie("viewed-"+channel.getChannelID(), Crypto.crypto().sign(channel.getStreamKey()), 86400);
    }

    /**
     * Method that checks if a user has already viewed a channel (in the last 24 hours).
     * 
     * @param ctx The HTTP context (User's cookies).
     * @param channel The {@link Channel} that the user viewed.
     * @return (@code true} if a user is viewed and {code false} if a user is not viewed.
     */
    public static boolean checkIfViewedChannel(Http.Context ctx, Channel channel){
        Http.Cookie cookie = request().cookie("viewed-"+channel.getChannelID());

        if(cookie!= null){
            if(!cookie.value().equals(Crypto.crypto().sign(channel.getStreamKey()))) { //matches!
                addViewedChannel(ctx, channel); //update cookie again
            }
            return true;
        }else{
            return false;
        }

    }

    /**
     * Method that adds a cookie to a user's session that verifies the user has successfully
     * entered in a password for a private {@link Channel}. Cookie expires after 2 hours. 
     *
     * Used with {@code checkIfAuthenticatedToChannel}.
     *
     * @param ctx The Http.Context of the user.
     * @param channel The private {@link Channel} that the User authenticated to.  
     */
    public static void addAuthenticatedChannel(Http.Context ctx, Channel channel){
        response().setCookie("auth-"+channel.getChannelID(), Crypto.crypto().sign(channel.getChannelPassword()), 7200);
    }

    /**
     * Method that checks of a user is authenticated to a private {@link Channel}.
     * 
     * @param ctx The Http.Context of the requesting user.
     * @param channel The private {@link Channel} that the user is requesting to access.
     * @return {@code true} if the user has previously authenticated to the channel (in the last 2 hours), {@code false} otherwise.
     */
    public static boolean checkIfAuthenticatedToChannel(Http.Context ctx, Channel channel){
        Http.Cookie cookie = request().cookie("auth-"+channel.getChannelID());

        if(cookie!= null){
            if(!cookie.value().equals(Crypto.crypto().sign(channel.getChannelPassword()))) { //matches!
                addAuthenticatedChannel(ctx, channel); //update cookie again
            }
            return true;
        }else{
            return false;
        }

    }

    /**
     * Gets the {@link User} model for the currently authenticated user.
     *
     * @param ctx The {@link play.mvc.Http.Context} of the user's session.
     * @return The {@link User} that represents the client.
     */
    public static User getUserInfo(Http.Context ctx){
        return (isLoggedIn(ctx))? User.findByUsername(getName(ctx)) : null;
    }
}
