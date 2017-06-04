package controllers;

import play.Play;
import play.api.libs.mailer.MailerClient;
import play.libs.mailer.Email;

import javax.inject.Inject;

/**
 * MailController: Controller to send the mail.
 *
 * @author Chandler Severson
 * @author Yiwei Zheng
 * @version 2.0
 * @since 1.0
 */
public class MailController {

    @Inject MailerClient mailerClient;

    /**
     * Helper Method to send an Email.
     *
     * @param subject The Subject of the Email.
     * @param userName The userName that the email is being sent to.
     * @param email The email address that the email should be sent to.
     * @param html The HTML that should be in the email.
     */
    public void sendMail(String subject, String userName, String email, String html){
        Email mail = new Email()
                .setSubject(subject)
                .setFrom("ZAStream Support <support@zastream.com>")
                .addTo(userName + " <"+email+">")
                .setBodyHtml(html);

        Play.application().injector().instanceOf(MailerClient.class).send(mail);
    }

}
