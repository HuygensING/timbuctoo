package nl.knaw.huygens.timbuctoo.storage.mongo;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.storage.DBIntegrationTestHelper;
import nl.knaw.huygens.timbuctoo.storage.EntityInducer;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class MongoDBIntegrationTestHelper implements DBIntegrationTestHelper {

  private static final int DB_PORT = 12345;
  private static final MongodStarter starter = MongodStarter.getDefaultInstance();
  private MongodExecutable mongodExe;
  private MongodProcess mongod;
  private MongoClient mongo;
  private MongoDB mongoDB;

  public MongoDBIntegrationTestHelper() {
    super();
  }

  /* (non-Javadoc)
   * @see nl.knaw.huygens.timbuctoo.storage.mongo.DBIntegrationTestHelper#startCleanDB()
   */
  @Override
  public void startCleanDB() throws Exception {
    mongodExe = starter.prepare(new MongodConfigBuilder().version(Version.Main.PRODUCTION)//
        .net(new Net(DB_PORT, Network.localhostIsIPv6()))//
        .build());

    mongod = mongodExe.start();
    mongo = new MongoClient("localhost", DB_PORT);

    mongoDB = new MongoDB(mongo, mongo.getDB("test"));
  }

  /* (non-Javadoc)
   * @see nl.knaw.huygens.timbuctoo.storage.mongo.DBIntegrationTestHelper#stopDB()
   */
  @Override
  public void stopDB() {
    mongod.stop();
    mongodExe.stop();
  }

  /* (non-Javadoc)
   * @see nl.knaw.huygens.timbuctoo.storage.mongo.DBIntegrationTestHelper#createStorage(nl.knaw.huygens.timbuctoo.config.TypeRegistry)
   */
  @Override
  public MongoStorage createStorage(TypeRegistry registry) throws ModelException {
    EntityInducer inducer = new EntityInducer(new MongoPropertyInducer());
    EntityReducer reducer = new EntityReducer(registry);
    return new MongoStorage(mongoDB, new EntityIds(registry, mongoDB), inducer, reducer);
  }

}
