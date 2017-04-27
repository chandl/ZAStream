package controllers;

import play.Play;
import play.api.libs.mailer.MailerClient;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.libs.mailer.Email;
import play.mvc.Result;
import views.formdata.FeedbackForm;

import javax.inject.Inject;

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

    public Result sendFeedBack () {
        DynamicForm dynform = Form.form().bindFromRequest();
        feedbackForm = formFactory.form(FeedbackForm.class);
        String email = dynform.get("email");
        String name = dynform.get("name");
        String message = dynform.get("message");
        int rate;
        fMail.sendMail("New Feedback From "+ name, "ZAStream Support","support@zastream.com", String.format(feedbackTemplate, name, email, message));
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
            "   </body>\n" +
            "</html>";



}
