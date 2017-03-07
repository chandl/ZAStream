package controllers;

import models.Channel;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import static play.mvc.Results.ok;

public class StreamController extends Controller{

//doesn't work with nginx, V2
    public Result stream(String name){
        User streamer = User.findByUsername(name);
        Channel c = streamer.findChannel();
//        Logger.debug("Got Channel: " + c.toString());
        String key = c.getStreamKey();

        return ok(new java.io.File("/HLS/live/"+key+"/index.m3u8"));
    }
}
