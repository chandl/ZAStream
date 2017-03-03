package controllers;
import models.User;
import play.api.inject.ApplicationLifecycle;
import play.db.DB;
import play.mvc.Controller;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

@Singleton
public class DatabaseController extends Controller{
    private static Connection connection;

    public DatabaseController() {
        this.connection = DB.getConnection();
    }
}
