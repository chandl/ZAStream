package controllers;

import helper.Secured;
import models.Channel;
import models.ChannelFactory;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Security;
import views.formdata.LoginForm;
import views.formdata.RegisterForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.*;
import javax.inject.Inject;
import java.util.List;

/**
 * AuthenticationController: Controller to handle Signing up & Logging in.
 *
 * @author Chandler Severson <seversonc@sou.edu>
 * @author Yiwei Zheng <zhengy1@sou.edu>
 * @version 1.0
 * @since 1.0
 */
public class AuthenticationController extends Controller {

    @Inject FormFactory formFactory;

    Form<RegisterForm> registerForm;
    Form<LoginForm> loginForm;

    /**
     * The registration page controller method. Shows the registration page.
     *
     * @return <code>HTTP OK</code> result.
     */
    public Result registerPage(){
        registerForm = formFactory.form(RegisterForm.class);
        return ok(register.render("Register", registerForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    /**
     * The controller method used to create new accounts.
     * <p>
     * Called from <code>POST /register</code>
     * </p>
     *
     * @return <code>badRequest</code> if there were errors in registration, <code>redirect</code> to home page if successful.
     */
    public Result newUser(){
        DynamicForm dynForm = Form.form().bindFromRequest();//Bind data from POST to the form
        registerForm = formFactory.form(RegisterForm.class);
        User newUser = new User(dynForm.get("userName"), dynForm.get("passWord"),dynForm.get("email") );

        //Use the User.validate() method to validate constraints
        List<ValidationError> errors = newUser.validate();
        if(errors == null){
            Channel newChannel = ChannelFactory.newChannel("PUB", newUser);

            //Add user & channel to DB
            newUser.save();
            newChannel.save();

            Logger.debug("Successful New User:" + newUser.getUserName());
            Logger.debug("Successful New Channel: "+newChannel.getStreamKey());
        }else{
            Logger.debug("Unsucessful New User: "+ newUser.getUserName());
            Logger.debug("Errors: "+errors.toString());

            //Add errors to registrationForm, these are passed back to the page
            for(ValidationError e : errors) {
                registerForm.reject(e.key(), e.message());
            }

            return badRequest(views.html.register.render("Registration Issues", registerForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }

        return redirect(controllers.routes.HomeController.index());
    }


    /**
     * The controller for the Sign In Page view
     *
     * <p>
     * Called from <code>GET /login</code>
     * </p>
     *
     * @return <code>HTTP OK</code> status with login form.
     */
    public Result loginPage(){
        loginForm = formFactory.form(LoginForm.class);
        return ok(login.render("Sign In", loginForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    /**
     * The controller for authenticating users.
     * <p>
     * Called from <code>POST /login</code>
     * </p>
     *
     * <p>
     * If successful, user will be authenticated to the site, otherwise,
     * they will be alerted to the issues in their input.
     * </p>
     *
     * @return <code>badRequest</code> if there were errors authenticating, <code>redirect</code> to Homepage if good.
     */
    public Result authenticate(){
        DynamicForm dynForm = Form.form().bindFromRequest();
        loginForm = formFactory.form(LoginForm.class);
        String user = dynForm.get("userName");
        String pass = dynForm.get("passWord");

        if(User.isValid(user, pass)){
            session().clear();
            session("userName", user);
            return redirect(routes.HomeController.index());

        }else if(!User.isUser(user)){//not a user
            loginForm.reject("userName", "Invalid Username");
            return badRequest(views.html.login.render("Invalid Username", loginForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }else{//bad password
            loginForm.reject("passWord", "Invalid Password");
            return badRequest(views.html.login.render("Invalid Password", loginForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }

    }

    /**
     * Logs the user out of their current session.
     * -Removes local session data.
     * <p>
     *     Called from <code>GET /logout</code>
     * </p>
     * @return <code>redirect</code> user to Homepage.
     */
    @Security.Authenticated(Secured.class)
    public Result logout(){
        session().clear();
        return redirect(routes.HomeController.index());
    }

}
