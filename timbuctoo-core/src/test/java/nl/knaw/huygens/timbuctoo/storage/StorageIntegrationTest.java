package nl.knaw.huygens.timbuctoo.storage;

import static nl.knaw.huygens.timbuctoo.storage.RelationTypeMatcher.matchesRelationType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoDBIntegrationTestHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

public class StorageIntegrationTest {
  private static final String OTHER_REGULAR_NAME = "otherRegularName";
  private static final String INVERSE_NAME = "tset";
  private static final String REGULAR_NAME = "test";
  private static final String REGULAR_NAME1 = "regularName1";
  private static final String REGULAR_NAME2 = "regularName2";
  private static final String INVERSE_NAME1 = "inverseName1";
  private static final String INVERSE_NAME2 = "inverseName2";
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

  /********************************************************************************
   * SystemEntity
   ********************************************************************************/

  @Test
  public void addSystemEntityAddsASystemEntityToTheStorageAndReturnsItsId() throws Exception {
    RelationType systemEntityToStore = createRelationType(REGULAR_NAME, INVERSE_NAME);

    String id = instance.addSystemEntity(RelationType.class, systemEntityToStore);

    assertThat(id, startsWith(RelationType.ID_PREFIX));

    assertThat(instance.getItem(RelationType.class, id), //
        matchesRelationType() //
            .withId(id)//
            .withInverseName(INVERSE_NAME)//
            .withRegularName(REGULAR_NAME));

  }

  private RelationType createRelationType(String regularName, String inverseName) {
    RelationType systemEntityToStore = new RelationType();
    systemEntityToStore.setRegularName(regularName);
    systemEntityToStore.setInverseName(inverseName);
    return systemEntityToStore;
  }

  @Test
  public void updateSystemEntityChangesTheExistingSystemEntity() throws Exception {
    RelationType systemEntityToStore = createRelationType(REGULAR_NAME, INVERSE_NAME);
    String id = instance.addSystemEntity(RelationType.class, systemEntityToStore);
    RelationType storedSystemEntity = instance.getItem(RelationType.class, id);
    assertThat(storedSystemEntity, is(notNullValue()));

    storedSystemEntity.setRegularName(OTHER_REGULAR_NAME);

    instance.updateSystemEntity(RelationType.class, storedSystemEntity);

    assertThat(instance.getItem(RelationType.class, id), //
        matchesRelationType() //
            .withId(id)//
            .withInverseName(INVERSE_NAME)//
            .withRegularName(OTHER_REGULAR_NAME));

  }

  @Test
  public void getSystemEntitiesReturnsAllTheSystemEntitiesOfACertainType() throws Exception {
    RelationType systemEntityToStore1 = createRelationType(REGULAR_NAME, INVERSE_NAME);
    String id1 = instance.addSystemEntity(RelationType.class, systemEntityToStore1);
    RelationType systemEntityToStore2 = createRelationType(REGULAR_NAME1, INVERSE_NAME1);
    String id2 = instance.addSystemEntity(RelationType.class, systemEntityToStore2);
    RelationType systemEntityToStore3 = createRelationType(REGULAR_NAME2, INVERSE_NAME2);
    String id3 = instance.addSystemEntity(RelationType.class, systemEntityToStore3);

    List<RelationType> storedSystemEntities = instance.getSystemEntities(RelationType.class).getAll();

    assertThat(storedSystemEntities.size(), is(equalTo(3)));
    List<RelationTypeMatcher> relationTypeMatchers = Lists.newArrayList(matchesRelationType()//
        .withId(id1)//
        .withInverseName(INVERSE_NAME)//
        .withRegularName(REGULAR_NAME),//
        matchesRelationType()//
            .withId(id2)//
            .withInverseName(INVERSE_NAME1)//
            .withRegularName(REGULAR_NAME1), //
        matchesRelationType()//
            .withId(id3)//
            .withInverseName(INVERSE_NAME2) //
            .withRegularName(REGULAR_NAME2));

    assertThat(storedSystemEntities, containsInAnyOrder(relationTypeMatchers.toArray(new RelationTypeMatcher[0])));
  }

  @Test
  public void deleteSystemEntityRemovesAnEntityFromTheDatabase() throws StorageException {
    RelationType systemEntityToStore = createRelationType(REGULAR_NAME, INVERSE_NAME);
    String id = instance.addSystemEntity(RelationType.class, systemEntityToStore);
    assertThat(instance.getItem(RelationType.class, id), is(notNullValue()));

    instance.deleteSystemEntity(RelationType.class, id);

    assertThat(instance.getItem(RelationType.class, id), is(nullValue()));
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
