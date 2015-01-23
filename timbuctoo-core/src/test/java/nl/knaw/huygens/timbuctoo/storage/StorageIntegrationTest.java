package nl.knaw.huygens.timbuctoo.storage;

import static nl.knaw.huygens.timbuctoo.storage.PersonMatcher.likePerson;
import static nl.knaw.huygens.timbuctoo.storage.PersonMatcher.likeProjectAPerson;
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
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Person.Gender;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoDBIntegrationTestHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import test.model.projecta.ProjectAPerson;

import com.google.common.collect.Lists;

public class StorageIntegrationTest {
  private static final Class<Person> PRIMITIVE_DOMAIN_ENTITY_TYPE = Person.class;
  private static final Change UPDATE_CHANGE = new Change();
  private static final Datable BIRTH_DATE2 = new Datable("18000312");
  private static final Class<ProjectAPerson> DOMAIN_ENTIY_TYPE = ProjectAPerson.class;
  private static final String PID = "pid";
  private static final Change CHANGE_TO_SAVE = UPDATE_CHANGE;
  private static final Datable DEATH_DATE = new Datable("19000101");
  private static final Datable BIRTH_DATE = new Datable("1800");
  private static final String PROJECT_A_PERSON_PROPERTY = "projectAPersonProperty";
  private static final Gender GENDER = Gender.MALE;
  private static final PersonName PERSON_NAME = PersonName.newInstance("Constantijn", "Huygens");
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
   * deleteDomainEntity
   * deleteNonPersistentDomainEntity
   * getDomainEntities
   * getAllVariations
   * getRevision
   * getItem 
   */

  @Test
  public void addDomainEntityAddsADomainEntityAndItsPrimitiveVersieToTheDatabase() throws Exception {
    ProjectAPerson domainEntityToStore = createPerson(GENDER, PERSON_NAME, PROJECT_A_PERSON_PROPERTY, BIRTH_DATE, DEATH_DATE);

    // action
    String id = instance.addDomainEntity(DOMAIN_ENTIY_TYPE, domainEntityToStore, CHANGE_TO_SAVE);

    assertThat(id, startsWith(Person.ID_PREFIX));

    List<PersonName> names = Lists.newArrayList(PERSON_NAME);
    assertThat("DomainEntity is not as expected", instance.getItem(DOMAIN_ENTIY_TYPE, id), //
        likeProjectAPerson() //
            .withProjectAPersonProperty(PROJECT_A_PERSON_PROPERTY) //
            .withId(id) //
            .withBirthDate(BIRTH_DATE) //
            .withDeathDate(DEATH_DATE) //
            .withGender(GENDER) //
            .withNames(names));

    assertThat("Primitive is not as expected", instance.getItem(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), //
        likePerson()//
            .withId(id) //
            .withNames(names)//
            .withGender(GENDER)//
            .withBirthDate(BIRTH_DATE)//
            .withDeathDate(DEATH_DATE));
  }

  @Test
  public void setPIDGivesTheDomainEntityAPidAndCreatesAVersion() throws Exception {
    String id = addDefaultProjectAPerson();
    // Make sure the entity exist
    assertThat(instance.getItem(DOMAIN_ENTIY_TYPE, id), is(notNullValue()));

    // action
    instance.setPID(DOMAIN_ENTIY_TYPE, id, PID);

    ProjectAPerson updatedEntity = instance.getItem(DOMAIN_ENTIY_TYPE, id);
    assertThat("Entity has no pid", updatedEntity.getPid(), is(equalTo(PID)));

    int rev = updatedEntity.getRev();
    assertThat(instance.getRevision(DOMAIN_ENTIY_TYPE, id, rev), //
        likeDefaultProjectAPerson(id)//
            .withRevision(rev));
  }

  @Test
  public void updateIncreasesTheRevisionNumberAndChangesTheDomainEntityButDoesNotCreateANewVersion() throws Exception {
    String id = addDefaultProjectAPerson();

    // Store the entity
    ProjectAPerson storedDomainEntity = instance.getItem(DOMAIN_ENTIY_TYPE, id);
    // Make sure the entity is stored
    assertThat(storedDomainEntity, is(notNullValue()));

    int firstRevision = storedDomainEntity.getRev();

    // Update The entity
    storedDomainEntity.setBirthDate(BIRTH_DATE2);
    instance.updateDomainEntity(DOMAIN_ENTIY_TYPE, storedDomainEntity, UPDATE_CHANGE);

    ProjectAPerson updatedEntity = instance.getItem(DOMAIN_ENTIY_TYPE, id);

    assertThat("Project domain entity is not updated", //
        updatedEntity, likeProjectAPerson()//
            .withProjectAPersonProperty(PROJECT_A_PERSON_PROPERTY)//
            .withBirthDate(BIRTH_DATE2)//
            .withDeathDate(DEATH_DATE)//
            .withGender(GENDER)//
            .withId(id)//
            .withNames(Lists.newArrayList(PERSON_NAME))//
            .withRevision(firstRevision + 1));

    assertThat("Primitive domain entity should not have changed", //
        instance.getItem(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), likeDefaultPerson(id));

    assertThat("No revision should be created for version 1",//
        instance.getRevision(DOMAIN_ENTIY_TYPE, id, firstRevision), is(nullValue()));

    int secondRevision = updatedEntity.getRev();
    assertThat("No revision should be created for version 2",//
        instance.getRevision(DOMAIN_ENTIY_TYPE, id, secondRevision), is(nullValue()));
  }

  @Ignore("Delete does not work. Test with multiple project variants.")
  @Test
  public void deletePersistentDomainEntityClearsTheEntityPropertiesSetsTheDeletedFlagToTrue() throws Exception {
    String id = addDefaultProjectAPerson();

    assertThat(instance.getItem(DOMAIN_ENTIY_TYPE, id), //
        likeDefaultProjectAPerson(id)//
            .withDeletedFlag(false));

    assertThat(instance.getItem(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), //
        likeDefaultPerson(id)//
            .withDeletedFlag(false));

    instance.deleteDomainEntity(DOMAIN_ENTIY_TYPE, id, UPDATE_CHANGE);

    int expectedRevision = 2;
    assertThat(instance.getItem(DOMAIN_ENTIY_TYPE, id), //
        likeDefaultProjectAPerson(id).withRevision(expectedRevision));

    assertThat(instance.getItem(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), //
        likeDefaultPerson(id).withRevision(expectedRevision));

  }

  private ProjectAPerson createPerson(Gender gender, PersonName name, String projectAPersonProperty, Datable birthDate, Datable deathDate) {
    ProjectAPerson domainEntityToStore = new ProjectAPerson();
    domainEntityToStore.setGender(gender);
    domainEntityToStore.addName(name);
    domainEntityToStore.setProjectAPersonProperty(projectAPersonProperty);
    domainEntityToStore.setBirthDate(birthDate);
    domainEntityToStore.setDeathDate(deathDate);

    return domainEntityToStore;
  }

  private String addDefaultProjectAPerson() throws StorageException {
    ProjectAPerson domainEntityToStore = createPerson(GENDER, PERSON_NAME, PROJECT_A_PERSON_PROPERTY, BIRTH_DATE, DEATH_DATE);
    String id = instance.addDomainEntity(DOMAIN_ENTIY_TYPE, domainEntityToStore, CHANGE_TO_SAVE);
    return id;
  }

  private PersonMatcher<Person> likeDefaultPerson(String id) {
    return likePerson()//
        .withBirthDate(BIRTH_DATE)//
        .withDeathDate(DEATH_DATE)//
        .withGender(GENDER)//
        .withId(id)//
        .withNames(Lists.newArrayList(PERSON_NAME));
  }

  private PersonMatcher<ProjectAPerson> likeDefaultProjectAPerson(String id) {
    return likeProjectAPerson()//
        .withProjectAPersonProperty(PROJECT_A_PERSON_PROPERTY)//
        .withBirthDate(BIRTH_DATE)//
        .withDeathDate(DEATH_DATE)//
        .withGender(GENDER)//
        .withId(id)//
        .withNames(Lists.newArrayList(PERSON_NAME));
  }
}
