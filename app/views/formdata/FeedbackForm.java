package views.formdata;

import play.data.validation.Constraints;

/**
 * Created by yiweizheng on 4/26/17.
 */
public class FeedbackForm {
    @Constraints.Required
    protected String email;

    @Constraints.Required
    protected String name;

    @Constraints.Required
    protected String message;

    @Constraints.Required
    protected int rate;

    public String getEmail() {
        return email;
    }

    public String message() { return message; }

    public String name() { return name; }

    public int rate() {return rate;}
}
