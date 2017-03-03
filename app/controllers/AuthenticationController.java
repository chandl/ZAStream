package controllers;

import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.mvc.Security;
import views.formdata.LoginForm;
import views.formdata.RegisterForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.*;
import javax.inject.Inject;
import java.util.List;

import static play.mvc.Controller.flash;
import static play.mvc.Controller.session;
import static play.mvc.Http.Context.Implicit.ctx;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

public class AuthenticationController {

    @Inject FormFactory formFactory;

    Form<RegisterForm> registerForm;
    Form<LoginForm> loginForm;

    public Result registerPage(){
        registerForm = formFactory.form(RegisterForm.class);
        return ok(register.render("register page ok", registerForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    public Result newUser(){
        DynamicForm dynForm = Form.form().bindFromRequest();
        registerForm = formFactory.form(RegisterForm.class);
        User newUser = new User();
        newUser.setUserName(dynForm.get("userName"));
        newUser.setPassWord(dynForm.get("passWord"));
        newUser.setEmail(dynForm.get("email"));

        List<ValidationError> errors = newUser.validate();
        if(errors == null){
            newUser.save();
            Logger.debug("Successful New User:" + newUser.getUserName());
        }else{
            Logger.debug("Unsucessful New User: "+ newUser.getUserName());
            Logger.debug("Errors: "+errors.toString());

            //Add errors to registrationForm
            for(ValidationError e : errors) {
                registerForm.reject(e.key(), e.message());
            }

            return badRequest(views.html.register.render("register page errors", registerForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }

        return redirect(controllers.routes.HomeController.index());
    }


    public Result loginPage(){
        loginForm = formFactory.form(LoginForm.class);
        return ok(login.render("login page ok", loginForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    public Result authenticate(){
        DynamicForm dynForm = Form.form().bindFromRequest();
        loginForm = formFactory.form(LoginForm.class);
        String user = dynForm.get("userName");
        String pass = dynForm.get("passWord");

        if(Secured.isValid(user, pass)){
            session().clear();
            session("userName", user);
            return redirect(routes.HomeController.index());

        }else if(!Secured.isUser(user)){//not a user
            loginForm.reject("userName", "Invalid Username");
            return badRequest(views.html.login.render("invalid username", loginForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }else{//bad password
            loginForm.reject("passWord", "Invalid Password");
            return badRequest(views.html.login.render("invalid password", loginForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }

    }

    @Security.Authenticated(Secured.class)
    public Result logout(){
        session().clear();
        return redirect(routes.HomeController.index());
    }

//    public Result testAddUser(){
//        User u = new User();
//        u.email = "chandler@zastream.com";
//        u.passWord = "helloWorld";
//        u.userName = "chandler";
//        u.save();
//
//
//        return redirect(controllers.routes.HomeController.index());
//    }

}
