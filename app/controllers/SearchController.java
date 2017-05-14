package controllers;

import helper.Secured;
import models.Channel;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;

public class SearchController extends Controller {


    public Result searchPage(String query){


        return ok(views.html.search.render(query, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), new ArrayList<Channel>()));
    }
}
