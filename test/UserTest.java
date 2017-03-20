import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebean.dbmigration.DdlGenerator;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import models.User;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.test.FakeApplication;
import play.test.Helpers;

import java.io.IOException;

/**
 * Created by chandler on 2/26/17.
 */
public class UserTest {

    public static FakeApplication app;
    public static DdlGenerator ddl;
    public static EbeanServer server;

    @BeforeClass
    public static void setUp() {
        app = (FakeApplication)Helpers.fakeApplication(Helpers.inMemoryDatabase());
        Helpers.start(app);

        server = Ebean.getServer("default");

        ServerConfig config = new ServerConfig();

        ddl = new DdlGenerator((SpiEbeanServer)server, config);
    }

    @AfterClass
    public static void stopApp(){
        Helpers.stop(app);
    }


}
