package controllers;

import play.Play;
import play.api.libs.mailer.MailerClient;
import play.libs.mailer.Email;

import javax.inject.Inject;

public class MailController {

    @Inject MailerClient mailerClient;

    public void sendMail(String subject, String userName, String email, String html){
        Email mail = new Email()
                .setSubject(subject)
                .setFrom("ZAStream Support <support@zastream.com>")
                .addTo(userName + " <"+email+">")
                .setBodyHtml(html);

        Play.application().injector().instanceOf(MailerClient.class).send(mail);
    }

}
