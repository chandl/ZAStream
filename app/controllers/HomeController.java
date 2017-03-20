package controllers;

import helper.Secured;
import play.mvc.*;
import views.html.*;

/**
 * HomeController: Controller to handle the Homepage.
 *
 * @author Chandler Severson <seversonc@sou.edu>
 * @author Yiwei Zheng <zhengy1@sou.edu>
 * @version 1.0
 * @since 1.0
 */
public class HomeController extends Controller {


    /**
     * Controller method to display the HomePage.
     *
     * @return <code>HTTP OK</code> result, rendering the Homepage.
     */
    public Result index() {
        return ok(index.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx())));
    }

}