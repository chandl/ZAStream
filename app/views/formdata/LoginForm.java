package views.formdata;


import play.data.validation.Constraints;

public class LoginForm {
    @Constraints.Required
    protected String userName;

    @Constraints.Required
    protected String passWord;

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
