package helper;

import models.User;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import static play.mvc.Controller.session;

/**
 * Secured: Class to handle Authentication and Session Storage
 *
 * @author Chandler Severson <seversonc@sou.edu>
 * @author Yiwei Zheng <zhengy1@sou.edu>
 * @version 1.0
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

    public static void authenticateUser(Http.Context ctx, String userName){
        ctx.session().clear();
        ctx.session().put("userName", userName);
        session("userName", userName);
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
