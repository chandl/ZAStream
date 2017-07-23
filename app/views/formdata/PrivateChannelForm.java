package views.formdata;

import play.data.validation.Constraints;

/**
 * Created by yiweizheng on 5/17/17.
 */
public class PrivateChannelForm {


    @Constraints.Required
    protected String channelPassword;

    public String getChannelPassword() {
        return channelPassword;
    }

    public void setChannelPassword(String channelPassword) {
        this.channelPassword = channelPassword;
    }



}
