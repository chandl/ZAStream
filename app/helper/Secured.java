package helper;

import controllers.routes;
import models.Channel;
import models.User;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import java.util.List;

import static play.mvc.Controller.session;

public class Secured extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context ctx) {
        return ctx.session().get("userName");
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return redirect(controllers.routes.HomeController.index());
    }

    public static String getEmail(Http.Context ctx){
        return ctx.session().get("email");
    }

    public static String getName(Http.Context ctx){
        return ctx.session().get("userName");
    }

    public static boolean isLoggedIn(Http.Context ctx){
        return (getName(ctx) != null);
    }

    public static User getUserInfo(Http.Context ctx){
        return (isLoggedIn(ctx))? (User)User.find.where().eq("userName", getName(ctx)).findList().get(0) : null;
    }

    public static boolean isUser(String name){
        List<User> users = User.find.where().eq("userName", name).findList();
//        users.forEach(user->Logger.debug(user.toString()));

        if(User.find.where().eq("userName", name).findRowCount() == 0){
            return false;
        }

        return true;
    }

    public static boolean emailExists(String mail){
        List<User> matchingEmails = User.find.where().eq("email", mail).findList();
//        matchingEmails.forEach(user->Logger.debug(user.toString()));

        if(User.find.where().eq("email", mail).findRowCount() == 0){
            return false;
        }
        return true;
    }

    public static boolean checkPassword(String userName, String passWord){
//        Logger.debug(String.format("In checkPassword. Username: %s, Password: %s", userName, passWord));

        User user = User.findByUsername(userName);

        return HashHelper.checkPassword(passWord, user.getPassWord());
    }


    public static boolean isValid(String userName, String password){
        return (userName != null && password != null) && isUser(userName) && checkPassword(userName, password);
    }


}
