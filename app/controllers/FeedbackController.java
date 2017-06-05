package controllers;

import helper.Secured;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import views.formdata.FeedbackForm;
import views.html.feedback;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FeedbackController: Controller to handle the Feedback page.
 *
 * @author Chandler Severson
 * @author Yiwei Zheng
 * @version 2.0
 * @since 2.0
 */
public class FeedbackController extends Controller{

    @Inject FormFactory formFactory;
    Form<FeedbackForm> feedbackForm;
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static MailController fMail = new MailController();

    /**
     * Controller method to show the Feedback Page.
     *
     * @return {@code HTTP.ok} the Feedback page.
     */
    public Result feedbackPage(){
        feedbackForm = formFactory.form(FeedbackForm.class);
        return ok(feedback.render("Share Your Thoughts!", feedbackForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    /**
     * Controller method to handle {@code POST}s to {@code /feedback}
     * @return {@code HTTP.badRequest } if there are validation errors, {@code HTTP.ok} otherwise
     */
    public Result sendFeedBack () {
        DynamicForm dynform = Form.form().bindFromRequest();
        feedbackForm = formFactory.form(FeedbackForm.class);
        String email = dynform.get("email");
        String name = dynform.get("name");
        String message = dynform.get("message");
        String rate = dynform.get("star");

        List<ValidationError> errors = validate(email, rate!=null);
        if(errors != null){
            for(ValidationError e : errors){
                feedbackForm.reject(e.key(), e.message());
                Logger.debug("ValidationError on Feedback Page: " + e.key());
            }

            flash("message", message);
            flash("email", email);
            flash("name", name);
            return badRequest(views.html.feedback.render("Feedback Issues...", feedbackForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
        }

        int rating = Integer.parseInt(rate);


        fMail.sendMail("New Feedback From "+ name, "ZAStream Support","support@zastream.com", String.format(feedbackTemplate, name, email, message, rating));
        return ok(views.html.feedback.render("Feedback Submitted!", feedbackForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    /**
     * Helper Method to validate the Feedback message.
     * Validates that the email is valid and that a rating was entered.
     *
     * @param email The email to validate.
     * @param rating If a rating was entered.
     * @return A List of ValidationErrors that were found while validating the parameters.
     */
    private List<ValidationError> validate(String email, boolean rating){
        List<ValidationError> errors = new ArrayList<ValidationError>();

        Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
        Matcher emailMatcher = emailPattern.matcher(email);

        if(!emailMatcher.matches()){
            errors.add(new ValidationError("emailInvalid", "Invalid e-mail entered."));
        }

        if(!rating){
            errors.add(new ValidationError("badRating", "No rating entered."));
        }

        return errors.isEmpty()? null : errors;
    }

    /**
     *  String that formats the Feedback Email from a user.
     */
    private static final String feedbackTemplate = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "   <head>\n" +
            "       <style>\n" +
            "         body{\n" +
            "             text-align:center;\n" +
            "         }\n" +
            "       </style>\n" +
            "   </head>\n" +
            "   <body>\n" +
            "       <h3> A new Feedback Message from <strong>%s</strong>(%s)</h3>\n" +
            "       <p>\n" +
            "           %s\n" +
            "       </p>\n" +
            "<p>Rating from 1 to 5: %d</p>\n"+
            "   </body>\n" +
            "</html>";



}
