package views.formdata;

import play.data.validation.Constraints;
import javax.validation.constraints.Size;

public class RegisterForm  {
    @Constraints.Required
    protected String email;

    @Constraints.Required
    protected String userName;

    @Constraints.Required
    @Size(min=6, max=64)
    protected String passWord;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}
