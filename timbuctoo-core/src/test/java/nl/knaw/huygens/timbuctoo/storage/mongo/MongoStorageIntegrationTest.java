package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.UnknownHostException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.EntityInducer;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.variation.model.projecta.ProjectADomainEntity;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class MongoStorageIntegrationTest {
  private static final MongodStarter starter = MongodStarter.getDefaultInstance();
  private MongodExecutable mongodExe;
  private MongodProcess mongod;
  private MongoClient mongo;
  private MongoStorage instance;

  @Before
  public void setUp() throws UnknownHostException, IOException, ModelException {
    mongodExe = starter.prepare(new MongodConfigBuilder().version(Version.Main.PRODUCTION)//
        .net(new Net(12345, Network.localhostIsIPv6()))//
        .build());

    mongod = mongodExe.start();
    mongo = new MongoClient("localhost", 12345);

    MongoDB mongoDB = new MongoDB(mongo, mongo.getDB("test"));

    TypeRegistry registry = TypeRegistry.getInstance();
    instance = new MongoStorage(mongoDB, new EntityIds(registry, mongoDB), new EntityInducer(), new EntityReducer(registry));
  }

  @After
  public void tearDown() {
    mongod.stop();
    mongodExe.stop();
  }

  @Test
  public void writeDomainEntityShouldAddAnItemToTheDatabase() throws StorageException {
    ProjectADomainEntity entity = new ProjectADomainEntity();
    entity.generalTestDocValue = "test";

    String id = instance.addDomainEntity(ProjectADomainEntity.class, entity, new Change());

    assertThat(entity.generalTestDocValue, is(equalTo(instance.getItem(ProjectADomainEntity.class, id).generalTestDocValue)));
  }
}
