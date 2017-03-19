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
            Logger.debug("New Channel Key: "+newChannel.getStreamKey());

            //Sending Email
            MailController mc = new MailController();
            mc.sendMail("Welcome to ZAStream, "+newUser.getUserName()+"!", newUser.getUserName(), newUser.getEmail(), getRegistrationEmail(newUser));
            Logger.debug("Sent Welcome Email to "+newUser.getEmail());
            //Log user into site.
            Secured.authenticateUser(ctx(), newUser.getUserName());
        }else{
            Logger.debug("Unsucessful New User: "+ newUser.getUserName());
            Logger.debug("Errors: "+errors.toString());

            //Add errors to registrationForm, these are passed back to the page
            for(ValidationError e : errors) {
                registerForm.reject(e.key(), e.message());
            }

            return badRequest(views.html.register.render("Registration Issues...", registerForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }

        return ok(views.html.register.render("Registration Success!", registerForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
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
            Secured.authenticateUser(ctx(), user);
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

    public String getRegistrationEmail(User newUser){
        String userName = newUser.getUserName();
        String streamKey = Channel.findChannel(newUser).getStreamKey();

        String registrationEmail = "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width\" />\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                "    <title>Simple Transactional Email</title>\n" +
                "    <style>\n" +
                "      /* -------------------------------------\n" +
                "          GLOBAL RESETS\n" +
                "      ------------------------------------- */\n" +
                "      img {\n" +
                "        border: none;\n" +
                "        -ms-interpolation-mode: bicubic;\n" +
                "        max-width: 100%; }\n" +
                "\n" +
                "      body {\n" +
                "        background-color: #f6f6f6;\n" +
                "        font-family: sans-serif;\n" +
                "        -webkit-font-smoothing: antialiased;\n" +
                "        font-size: 14px;\n" +
                "        line-height: 1.4;\n" +
                "        margin: 0;\n" +
                "        padding: 0;\n" +
                "        -ms-text-size-adjust: 100%;\n" +
                "        -webkit-text-size-adjust: 100%; }\n" +
                "\n" +
                "      table {\n" +
                "        border-collapse: separate;\n" +
                "        mso-table-lspace: 0pt;\n" +
                "        mso-table-rspace: 0pt;\n" +
                "        width: 100%; }\n" +
                "        table td {\n" +
                "          font-family: sans-serif;\n" +
                "          font-size: 14px;\n" +
                "          vertical-align: top; }\n" +
                "\n" +
                "      /* -------------------------------------\n" +
                "          BODY & CONTAINER\n" +
                "      ------------------------------------- */\n" +
                "\n" +
                "      .body {\n" +
                "        background-color: #f6f6f6;\n" +
                "        width: 100%; }\n" +
                "\n" +
                "      /* Set a max-width, and make it display as block so it will automatically stretch to that width, but will also shrink down on a phone or something */\n" +
                "      .container {\n" +
                "        display: block;\n" +
                "        Margin: 0 auto !important;\n" +
                "        /* makes it centered */\n" +
                "        max-width: 580px;\n" +
                "        padding: 10px;\n" +
                "        width: 580px; }\n" +
                "\n" +
                "      /* This should also be a block element, so that it will fill 100% of the .container */\n" +
                "      .content {\n" +
                "        box-sizing: border-box;\n" +
                "        display: block;\n" +
                "        Margin: 0 auto;\n" +
                "        max-width: 580px;\n" +
                "        padding: 10px; }\n" +
                "\n" +
                "      .monospace{\n" +
                "        font-family: 'DejaVu Sans Mono', monospace;\n" +
                "        border: 1px solid black;\n" +
                "        padding: 5px;\n" +
                "        font-weight: 700;\n" +
                "      }\n" +
                "\n" +
                "      .highlight{\n" +
                "        color: red;\n" +
                "      }\n" +
                "      /* -------------------------------------\n" +
                "          HEADER, FOOTER, MAIN\n" +
                "      ------------------------------------- */\n" +
                "      .main {\n" +
                "        background: #fff;\n" +
                "        border-radius: 3px;\n" +
                "        width: 100%; }\n" +
                "\n" +
                "      .wrapper {\n" +
                "        box-sizing: border-box;\n" +
                "        padding: 20px; }\n" +
                "\n" +
                "      .footer {\n" +
                "        clear: both;\n" +
                "        padding-top: 10px;\n" +
                "        text-align: center;\n" +
                "        width: 100%; }\n" +
                "        .footer td,\n" +
                "        .footer p,\n" +
                "        .footer span,\n" +
                "        .footer a {\n" +
                "          color: #999999;\n" +
                "          font-size: 12px;\n" +
                "          text-align: center; }\n" +
                "\n" +
                "      /* -------------------------------------\n" +
                "          TYPOGRAPHY\n" +
                "      ------------------------------------- */\n" +
                "      h1,\n" +
                "      h2,\n" +
                "      h3,\n" +
                "      h4 {\n" +
                "        color: #000000;\n" +
                "        font-family: sans-serif;\n" +
                "        font-weight: 400;\n" +
                "        line-height: 1.4;\n" +
                "        margin: 0;\n" +
                "        Margin-bottom: 30px; }\n" +
                "\n" +
                "      h1 {\n" +
                "        font-size: 35px;\n" +
                "        font-weight: 300;\n" +
                "        text-align: center;\n" +
                "        text-transform: capitalize; }\n" +
                "\n" +
                "      p,\n" +
                "      ul,\n" +
                "      ol {\n" +
                "        font-family: sans-serif;\n" +
                "        font-size: 14px;\n" +
                "        font-weight: normal;\n" +
                "        margin: 0;\n" +
                "        Margin-bottom: 15px; }\n" +
                "        p li,\n" +
                "        ul li,\n" +
                "        ol li {\n" +
                "          list-style-position: inside;\n" +
                "          margin-left: 5px; }\n" +
                "\n" +
                "      a {\n" +
                "        color: #3498db;\n" +
                "        text-decoration: underline; }\n" +
                "\n" +
                "      /* -------------------------------------\n" +
                "          BUTTONS\n" +
                "      ------------------------------------- */\n" +
                "      .btn {\n" +
                "        box-sizing: border-box;\n" +
                "        width: 100%; }\n" +
                "        .btn > tbody > tr > td {\n" +
                "          padding-bottom: 15px; }\n" +
                "        .btn table {\n" +
                "          width: auto; }\n" +
                "        .btn table td {\n" +
                "          background-color: #ffffff;\n" +
                "          border-radius: 5px;\n" +
                "          text-align: center; }\n" +
                "        .btn a {\n" +
                "          background-color: #ffffff;\n" +
                "          border: solid 1px #3498db;\n" +
                "          border-radius: 5px;\n" +
                "          box-sizing: border-box;\n" +
                "          color: #3498db;\n" +
                "          cursor: pointer;\n" +
                "          display: inline-block;\n" +
                "          font-size: 14px;\n" +
                "          font-weight: bold;\n" +
                "          margin: 0;\n" +
                "          padding: 12px 25px;\n" +
                "          text-decoration: none;\n" +
                "          text-transform: capitalize; }\n" +
                "\n" +
                "      .btn-primary table td {\n" +
                "        background-color: #3498db; }\n" +
                "\n" +
                "      .btn-primary a {\n" +
                "        background-color: #3498db;\n" +
                "        border-color: #3498db;\n" +
                "        color: #ffffff; }\n" +
                "\n" +
                "      /* -------------------------------------\n" +
                "          OTHER STYLES THAT MIGHT BE USEFUL\n" +
                "      ------------------------------------- */\n" +
                "      .last {\n" +
                "        margin-bottom: 0; }\n" +
                "\n" +
                "      .first {\n" +
                "        margin-top: 0; }\n" +
                "\n" +
                "      .align-center {\n" +
                "        text-align: center; }\n" +
                "\n" +
                "      .align-right {\n" +
                "        text-align: right; }\n" +
                "\n" +
                "      .align-left {\n" +
                "        text-align: left; }\n" +
                "\n" +
                "      .clear {\n" +
                "        clear: both; }\n" +
                "\n" +
                "      .mt0 {\n" +
                "        margin-top: 0; }\n" +
                "\n" +
                "      .mb0 {\n" +
                "        margin-bottom: 0; }\n" +
                "\n" +
                "      .preheader {\n" +
                "        color: transparent;\n" +
                "        display: none;\n" +
                "        height: 0;\n" +
                "        max-height: 0;\n" +
                "        max-width: 0;\n" +
                "        opacity: 0;\n" +
                "        overflow: hidden;\n" +
                "        mso-hide: all;\n" +
                "        visibility: hidden;\n" +
                "        width: 0; }\n" +
                "\n" +
                "      .powered-by a {\n" +
                "        text-decoration: none; }\n" +
                "\n" +
                "      hr {\n" +
                "        border: 0;\n" +
                "        border-bottom: 1px solid #f6f6f6;\n" +
                "        Margin: 20px 0; }\n" +
                "\n" +
                "      /* -------------------------------------\n" +
                "          RESPONSIVE AND MOBILE FRIENDLY STYLES\n" +
                "      ------------------------------------- */\n" +
                "      @media only screen and (max-width: 620px) {\n" +
                "        table[class=body] h1 {\n" +
                "          font-size: 28px !important;\n" +
                "          margin-bottom: 10px !important; }\n" +
                "        table[class=body] p,\n" +
                "        table[class=body] ul,\n" +
                "        table[class=body] ol,\n" +
                "        table[class=body] td,\n" +
                "        table[class=body] span,\n" +
                "        table[class=body] a {\n" +
                "          font-size: 16px !important; }\n" +
                "        table[class=body] .wrapper,\n" +
                "        table[class=body] .article {\n" +
                "          padding: 10px !important; }\n" +
                "        table[class=body] .content {\n" +
                "          padding: 0 !important; }\n" +
                "        table[class=body] .container {\n" +
                "          padding: 0 !important;\n" +
                "          width: 100% !important; }\n" +
                "        table[class=body] .main {\n" +
                "          border-left-width: 0 !important;\n" +
                "          border-radius: 0 !important;\n" +
                "          border-right-width: 0 !important; }\n" +
                "        table[class=body] .btn table {\n" +
                "          width: 100% !important; }\n" +
                "        table[class=body] .btn a {\n" +
                "          width: 100% !important; }\n" +
                "        table[class=body] .img-responsive {\n" +
                "          height: auto !important;\n" +
                "          max-width: 100% !important;\n" +
                "          width: auto !important; }}\n" +
                "\n" +
                "      /* -------------------------------------\n" +
                "          PRESERVE THESE STYLES IN THE HEAD\n" +
                "      ------------------------------------- */\n" +
                "      @media all {\n" +
                "        .ExternalClass {\n" +
                "          width: 100%; }\n" +
                "        .ExternalClass,\n" +
                "        .ExternalClass p,\n" +
                "        .ExternalClass span,\n" +
                "        .ExternalClass font,\n" +
                "        .ExternalClass td,\n" +
                "        .ExternalClass div {\n" +
                "          line-height: 100%; }\n" +
                "        .apple-link a {\n" +
                "          color: inherit !important;\n" +
                "          font-family: inherit !important;\n" +
                "          font-size: inherit !important;\n" +
                "          font-weight: inherit !important;\n" +
                "          line-height: inherit !important;\n" +
                "          text-decoration: none !important; }\n" +
                "        .btn-primary table td:hover {\n" +
                "          background-color: #34495e !important; }\n" +
                "        .btn-primary a:hover {\n" +
                "          background-color: #34495e !important;\n" +
                "          border-color: #34495e !important; } }\n" +
                "\n" +
                "    </style>\n" +
                "  </head>\n" +
                "  <body class=\"\">\n" +
                "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"body\">\n" +
                "      <tr>\n" +
                "        <td>&nbsp;</td>\n" +
                "        <td class=\"container\">\n" +
                "          <div class=\"content\">\n" +
                "\n" +
                "            <!-- START CENTERED WHITE CONTAINER -->\n" +
                "            <span class=\"preheader\">Welcome to ZAStream!</span>\n" +
                "            <table class=\"main\">\n" +
                "\n" +
                "              <!-- START MAIN CONTENT AREA -->\n" +
                "              <tr>\n" +
                "                <td class=\"wrapper\">\n" +
                "                  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "                    <tr>\n" +
                "                      <td>\n" +
                "                        <img src=\"http://zastream.com/big-logo.png\"/>\n" +
                "                        <h2>Hi "+userName+",</h2>\n" +
                "                        <p>Welcome to ZAStream! A new live video streaming website built for professionals, teachers, gamers, and everyone in-between.</p>\n" +
                "\n" +
                "                        <p>Want to get started Streaming Today but aren't sure how? Check out the link below to see how to use our favorite streaming software (OBS) (it's free!).</p>\n" +
                "\n" +
                "                        <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"btn btn-primary\">\n" +
                "                          <tbody>\n" +
                "                            <tr>\n" +
                "                              <td align=\"center\">\n" +
                "                                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "                                  <tbody>\n" +
                "                                    <tr>\n" +
                "                                      <td> <a href=\"https://obsproject.com/forum/resources/obs-classic-official-overview-guide.6/\" target=\"_blank\">Open Broadcaster Software</a> </td>\n" +
                "                                    </tr>\n" +
                "                                  </tbody>\n" +
                "                                </table>\n" +
                "                              </td>\n" +
                "                            </tr>\n" +
                "                          </tbody>\n" +
                "                        </table>\n" +
                "\n" +
                "                        <p>Already an Experienced Streamer? Configure your stream with the following settings: </p>\n" +
                "\n" +
                "                        <p class=\"monospace\">\n" +
                "                          Stream Type: <span class=\"highlight\">Custom Streaming Server</span> <br/>\n" +
                "                          URL: <span class=\"highlight\">rtmp://dev.zastream.com/live</span> <br/>\n" +
                "                          Stream Key: <span class=\"highlight\">"+streamKey+"</span> <br/>\n" +
                "                        </p>\n" +
                "\n" +
                "                        <p>We really appreciate you testing our website and would love any feedback. Please send any comments, questions, or concerns to <a href=\"mailto: support@zastream.com\">support@zastream.com</a>.</p>\n" +
                "\n" +
                "                        <p>Once again, thanks for using ZAStream. We hope you enjoy your experience</p>\n" +
                "\n" +
                "                        <p>\n" +
                "                          Thanks,<br/>\n" +
                "                          ZAStream Team\n" +
                "                        </p>\n" +
                "                      </td>\n" +
                "                    </tr>\n" +
                "                  </table>\n" +
                "                </td>\n" +
                "              </tr>\n" +
                "\n" +
                "              <!-- END MAIN CONTENT AREA -->\n" +
                "              </table>\n" +
                "\n" +
                "            <!-- START FOOTER -->\n" +
                "            <div class=\"footer\">\n" +
                "              <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "                <tr>\n" +
                "                  <td class=\"content-block\">\n" +
                "                    <span class=\"apple-link\">Copyright ZAStream &copy; 2017</span>\n" +
                "                    <br> Don't like these emails? <a href=\"#\">Unsubscribe</a>.\n" +
                "                  </td>\n" +
                "                </tr>\n" +
                "              </table>\n" +
                "            </div>\n" +
                "\n" +
                "            <!-- END FOOTER -->\n" +
                "\n" +
                "<!-- END CENTERED WHITE CONTAINER --></div>\n" +
                "        </td>\n" +
                "        <td>&nbsp;</td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>\n";
        return registrationEmail;
    }
}
