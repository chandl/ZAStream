package views.formdata;

import play.data.validation.Constraints;

import javax.validation.constraints.Size;

/**
 * Created by yiweizheng on 5/3/17.
 */
public class PasswordRecoveryForm {
    @Constraints.Required
    protected String email;

    @Constraints.Required
    @Size(min=6, max=64)
    protected String newPassword;

    @Constraints.Required
    @Size(min=6, max=64)
    protected String confirmPassword;



    public String getEmail() {
        return email;
    }

    public String getNewPassword() {return newPassword;}

    public String getConfirmPassword() {return confirmPassword;}

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
