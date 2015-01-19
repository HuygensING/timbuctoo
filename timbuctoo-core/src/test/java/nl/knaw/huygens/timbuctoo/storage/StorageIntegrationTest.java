package nl.knaw.huygens.timbuctoo.storage;

import static nl.knaw.huygens.timbuctoo.storage.RelationTypeMatcher.matchesRelationType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoDBIntegrationTestHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StorageIntegrationTest {
  private Storage instance;
  private static TypeRegistry typeRegistry;
  private MongoDBIntegrationTestHelper dbIntegrationTestHelper;

  @BeforeClass
  public static void createTypeRegistry() throws Exception {
    typeRegistry = TypeRegistry.getInstance();
    typeRegistry.init("nl.knaw.huygens.timbuctoo.model.*");

  }

  @Before
  public void setUp() throws Exception {
    dbIntegrationTestHelper = new MongoDBIntegrationTestHelper();

    dbIntegrationTestHelper.startCleanDB();
    instance = dbIntegrationTestHelper.createStorage(typeRegistry);
  }

  @After
  public void tearDown() {
    dbIntegrationTestHelper.stopDB();
  }

  /*
   * Methods to test for SystemEntity:
   * 
   * addSystemEntity
   * updateSystemEntity
   * getSystemEntities
   * deleteSystemEntity
   * getItem
   */

  @Test
  public void addSystemEntityAddsASystemEntityToTheStorageAndReturnsItsId() throws Exception {
    RelationType systemEntityToStore = new RelationType();
    String regularName = "test";
    systemEntityToStore.setRegularName(regularName);
    String inverseName = "tset";
    systemEntityToStore.setInverseName(inverseName);

    String id = instance.addSystemEntity(RelationType.class, systemEntityToStore);

    assertThat(id, startsWith(RelationType.ID_PREFIX));

    assertThat(instance.getItem(RelationType.class, id), //
        matchesRelationType() //
            .withId(id)//
            .withInverseName(inverseName)//
            .withRegularName(regularName));

  }
  /* 
   * Methods to test for DomainEntity:
   * 
   * addDomainEntity
   * setPid
   * updateDomainEntity
   * deleteDomainEntity
   * deleteNonPersistentDomainEntity
   * getDomainEntities
   * getAllVariations
   * getRevision
   * getItem 
   */

}
