package controllers;

import assets.constant.Constants;
import helper.HashHelper;
import helper.Secured;
import models.PasswordRecovery;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import views.formdata.PasswordRecoveryForm;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AccountRecoveryController
 */
public class AccountRecoveryController extends Controller {
    @Inject
    FormFactory formFactory;
    Form<PasswordRecoveryForm> passwordRecoveryForm;

    /**
     * The main recovery page - shows a form for entering email address.
     * @return
     */
    public Result recoveryPage(){
        passwordRecoveryForm = formFactory.form(PasswordRecoveryForm.class);
        return ok(views.html.account_recovery.render("Recover your Password", passwordRecoveryForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    /**
     * The method called when we POST on the main recovery page (with email input)
     * Sends an email to the user with a recovery key.
     *
     * @return
     */
    public Result sendRecovery(){
        DynamicForm dynForm = Form.form().bindFromRequest();
        passwordRecoveryForm = formFactory.form(PasswordRecoveryForm.class);

        String email = dynForm.get("email");

        Pattern emailPattern = Pattern.compile(User.EMAIL_PATTERN);
        Matcher emailMatcher = emailPattern.matcher(email);

        User user = User.findByEmail(email);

        //Bad email
        if(!emailMatcher.matches() || !User.emailExists(email) || user == null){
            ValidationError error = new ValidationError("emailInvalid", "Invalid e-mail entered.");
            flash("email", email);
            passwordRecoveryForm.reject(error);

            return badRequest(views.html.account_recovery.render("Recovery Issues...", passwordRecoveryForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }

        PasswordRecovery recovery = new PasswordRecovery(user);
        recovery.save();
        String html = getRecoveryEmail(Constants.RECOVERY_LINK , recovery);

        MailController mc = new MailController();
        mc.sendMail("ZAStream Password Recovery", user.getUserName(), email, html);
        Logger.debug("Sent Password Recovery Email to : "+ user.getUserName());
        return ok(views.html.account_recovery.render("Recovery Email Sent!", passwordRecoveryForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()) ));

    }

    /**
     * Method called when we do a GET on /recover-password/recoveryHash
     * Checks if the recovery hash is valid; If it is, it displays a form to choose & confirm a new password.
     *
     * @param recoveryHash
     * @return
     */
    public Result newPassword(String recoveryHash){
        passwordRecoveryForm = formFactory.form(PasswordRecoveryForm.class);
        PasswordRecovery recovery;
        if( (recovery = PasswordRecovery.findByHash(recoveryHash)) == null || recovery.isExpired()){ // bad recovery key
            return badRequest(views.html.account_recovery.render("Invalid Recovery Key!", passwordRecoveryForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }

        return ok(views.html.account_recovery.render("Reset Your Password", passwordRecoveryForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    /**
     * Method called when we POST on /recover-password/recoveryHash
     * Actually changes the User's password.
     *
     * @param recoveryHash
     * @return
     */
    public Result changePassword(String recoveryHash){

        recoveryHash = recoveryHash.substring(recoveryHash.lastIndexOf("/")+1, recoveryHash.length());
        Logger.info("changePassword. recoveryHash: "+ recoveryHash);

        DynamicForm dynForm = Form.form().bindFromRequest();
        passwordRecoveryForm = formFactory.form(PasswordRecoveryForm.class);

        String newPassword = dynForm.get("newPassWord");
        String confirmPassword = dynForm.get("confirmPassword");

        if(!newPassword.equals(confirmPassword)){
            ValidationError error = new ValidationError("passwordInvalid", "Passwords Did Not Match. Try again!");
            passwordRecoveryForm.reject(error);
            return badRequest(views.html.account_recovery.render("Reset Your Password", passwordRecoveryForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }

        if(newPassword.length()<6 || newPassword.length()>64) {
            ValidationError error = new ValidationError("passwordLengthInvalid", "Passwords is invaild size. Try again!");
            passwordRecoveryForm.reject(error);
            return badRequest(views.html.account_recovery.render("Reset Your Password", passwordRecoveryForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));

        }

        PasswordRecovery recovery = PasswordRecovery.findByHash(recoveryHash);
        User user = recovery.getUserToRecover();


        Logger.debug(String.format("User: %s just reset their password with Recovery Hash: %s", user.getUserName(), recovery.getRecoveryHash()));

        //Update user's password
        user.setPassWord(HashHelper.createPassword(newPassword));
        user.save();

        //Delete the recovery key from the DB
        recovery.delete();

        //Send user confirmation email.
        String html = getRecoverySuccessEmail(Constants.RECOVERY_LINK, user);
        MailController mc = new MailController();
        mc.sendMail("ZAStream Account - Password Changed", user.getUserName(), user.getEmail(), html);

        return ok(views.html.account_recovery.render("Reset Your Password - Password Changed Successfully", passwordRecoveryForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }


    /**
     * Helper method to create a recovery email.
     *
     * @param link The link to the recovery page. (E.g. http://localhost:9000/recover-password/)
     * @param recovery
     * @return
     */
    private String getRecoveryEmail(String link, PasswordRecovery recovery){
        String passwordResetEmail = "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width\" />\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
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
                "        img.full {\n" +
                "               width:50%;\n" +
                "               height:auto;\n" +
                "        }\n" +
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
                "            <span class=\"preheader\">ZAStream Account - Password Reset</span>\n" +
                "            <table class=\"main\">\n" +
                "\n" +
                "              <!-- START MAIN CONTENT AREA -->\n" +
                "              <tr>\n" +
                "                <td class=\"wrapper\">\n" +
                "                  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "                    <tr>\n" +
                "                      <td>\n" +
                "                        <img src=\"http://zastream.com/assets/big-logo.png\" class=\"full\"/>\n" +
                "                        <hr/>\n" +
                "                        <h2>Hi "+ recovery.getUserToRecover().getUserName()+",</h2>\n" +
                "                        <p>We have received a request to reset your password. Please <a href=\""+link + recovery.getRecoveryHash()+"\">click here to confirm the reset</a> to choose a new password. <br/>Otherwise, you can ignore this email.</p>\n" +
                "\n" +
                "                        <p>Please note that this link will expire in <b>2 hours</b>.</p>\n" +
                "\n" +
                "                        <br/>\n" +
                "\n" +
                "                        <p>\n" +
                "                          Thanks,<br/>\n" +
                "                          ZAStream Team\n" +
                "                        </p>\n" +
                "\n" +
                "                        <p><small>If you have any comments, questions, or concerns, please send them to <a href=\"mailto: support@zastream.com\">support@zastream.com</a>.</small></p>\n" +
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

        return passwordResetEmail;
    }

    private String getRecoverySuccessEmail(String link, User user){
        String successEmail = "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width\" />\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
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
                "        img.full {\n" +
                "               width:50%;\n" +
                "               height:auto;\n" +
                "        }\n" +
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
                "            <span class=\"preheader\">ZAStream Account - Password Reset</span>\n" +
                "            <table class=\"main\">\n" +
                "\n" +
                "              <!-- START MAIN CONTENT AREA -->\n" +
                "              <tr>\n" +
                "                <td class=\"wrapper\">\n" +
                "                  <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "                    <tr>\n" +
                "                      <td>\n" +
                "                        <img src=\"http://zastream.com/assets/big-logo.png\" class=\"full\"/>\n" +
                "                        <hr/>\n" +
                "                        <h2>Hi "+user.getUserName()+",</h2>\n" +
                "                        <p>This is a confirmation that the password for your ZAStream account " + user.getUserName() + " has just been changed. <br/></p>\n" +
                "\n" +
                "                        <p>If this is your ZAStream account but you didn't request a password change, you should <a href=\""+link+"\">request a new password here</a>. </p>\n" +
                "\n" +
                "                        <p>Otherwise, you can ignore this email.</p>\n" +
                "                        <br/>\n" +
                "                        <p>\n" +
                "                          Thanks,<br/>\n" +
                "                          ZAStream Team\n" +
                "                        </p>\n" +
                "\n" +
                "                        <p><small>If you have any comments, questions, or concerns, please send them to <a href=\"mailto: support@zastream.com\">support@zastream.com</a>.</small></p>\n" +
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

        return successEmail;
    }

}
