package nl.knaw.huygens.timbuctoo.tools.other;

import java.net.UnknownHostException;

import nl.knaw.huygens.timbuctoo.config.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

/**
 * Manages the Mongo database.
 */
public class MongoAdmin {

  private static final Logger LOG = LoggerFactory.getLogger(MongoAdmin.class);

  private Mongo mongo;
  private String dbName;
  private DB db;

  public MongoAdmin(Configuration config) throws UnknownHostException {
    MongoOptions options = new MongoOptions();
    options.safe = true;

    String host = config.getSetting("database.host", "localhost");
    int port = config.getIntSetting("database.port", 27017);
    mongo = new Mongo(new ServerAddress(host, port), options);

    dbName = config.getSetting("database.name");
    db = mongo.getDB(dbName);

    String user = config.getSetting("database.user");
    if (!user.isEmpty()) {
      String password = config.getSetting("database.password");
      db.authenticate(user, password.toCharArray());
    }
  }

  public void dropDatabase() {
    try {
      LOG.info("Dropping database '{}'", dbName);
      db.dropDatabase();
      LOG.info("Dropped database");
    } catch (MongoException e) {
      LOG.error(e.getMessage());
    } finally {
      mongo.close();
      LOG.info("Closed");
      db = null;
    }
  }

}
