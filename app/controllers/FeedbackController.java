package controllers;

import helper.Secured;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.formdata.FeedbackForm;
import views.html.feedback;

import javax.inject.Inject;

import static play.mvc.Http.Context.Implicit.ctx;
import static play.mvc.Results.ok;

/**
 * FeedbackController: Controller to handle the Feedback page.
 *
 * @author Chandler Severson <seversonc@sou.edu>
 * @author Yiwei Zheng <zhengy1@sou.edu>
 * @version 2.0
 * @since 2.0
 */
public class FeedbackController {

    @Inject FormFactory formFactory;
    Form<FeedbackForm> feedbackForm;

    private static MailController fMail = new MailController();

    public Result feedbackPage(){
        feedbackForm = formFactory.form(FeedbackForm.class);
        return ok(feedback.render("Share Your Thoughts!", feedbackForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

    public Result sendFeedBack () {
        DynamicForm dynform = Form.form().bindFromRequest();
        feedbackForm = formFactory.form(FeedbackForm.class);
        String email = dynform.get("email");
        String name = dynform.get("name");
        String message = dynform.get("message");

        String rate = dynform.get("star");
        int rating = Integer.parseInt(rate);

        fMail.sendMail("New Feedback From "+ name, "ZAStream Support","support@zastream.com", String.format(feedbackTemplate, name, email, message, rating));
        return ok(views.html.feedback.render("Feedback Submitted!", feedbackForm, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

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
