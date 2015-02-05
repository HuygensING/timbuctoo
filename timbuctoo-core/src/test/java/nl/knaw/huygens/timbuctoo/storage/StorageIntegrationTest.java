package nl.knaw.huygens.timbuctoo.storage;

import static nl.knaw.huygens.timbuctoo.storage.PersonMatcher.likePerson;
import static nl.knaw.huygens.timbuctoo.storage.PersonMatcher.likeProjectAPerson;
import static nl.knaw.huygens.timbuctoo.storage.RelationTypeMatcher.matchesRelationType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
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

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import test.model.projecta.ProjectAPerson;

import com.google.common.collect.Lists;

public abstract class StorageIntegrationTest {
  // General constants
  private static final Change CHANGE_TO_SAVE = new Change();
  private static final Change UPDATE_CHANGE = new Change();

  // SystemEntity constants
  private static final String REGULAR_NAME = "test";
  private static final String REGULAR_NAME1 = "regularName1";
  private static final String REGULAR_NAME2 = "regularName2";
  private static final String INVERSE_NAME = "tset";
  private static final String INVERSE_NAME1 = "inverseName1";
  private static final String INVERSE_NAME2 = "inverseName2";
  private static final String OTHER_REGULAR_NAME = "otherRegularName";

  // DomainEntity constants
  private static final Class<ProjectAPerson> DOMAIN_ENTITY_TYPE = ProjectAPerson.class;
  private static final Class<Person> PRIMITIVE_DOMAIN_ENTITY_TYPE = Person.class;
  private static final Datable BIRTH_DATE = new Datable("1800");
  private static final Datable BIRTH_DATE1 = new Datable("10001213");
  private static final Datable BIRTH_DATE2 = new Datable("18000312");
  private static final Datable DEATH_DATE = new Datable("19000101");
  private static final Datable DEATH_DATE1 = new Datable("11000201");
  private static final Datable DEATH_DATE2 = new Datable("19020103");
  private static final Gender GENDER = Gender.MALE;
  private static final Gender GENDER1 = Gender.FEMALE;
  private static final Gender GENDER2 = Gender.MALE;
  private static final PersonName PERSON_NAME = PersonName.newInstance("Constantijn", "Huygens");
  private static final PersonName PERSON_NAME1 = PersonName.newInstance("Maria", "Reigersberch");
  private static final PersonName PERSON_NAME2 = PersonName.newInstance("James", "Petiver");
  private static final String PID = "pid";
  private static final String PROJECT_A_PERSON_PROPERTY = "projectAPersonProperty";
  private static final String PROJECT_A_PERSON_PROPERTY1 = "projectAPersonProperty1";
  private static final String PROJECT_A_PERSON_PROPERTY2 = "projectAPersonProperty2";

  private Storage instance;
  private static TypeRegistry typeRegistry;
  private DBIntegrationTestHelper dbIntegrationTestHelper;

  @BeforeClass
  public static void createTypeRegistry() throws Exception {

    typeRegistry = TypeRegistry.getInstance();
    typeRegistry.init("nl.knaw.huygens.timbuctoo.model.* test.model.projecta");

  }

  @Before
  public void setUp() throws Exception {
    dbIntegrationTestHelper = createDBIntegrationTestHelper();
    dbIntegrationTestHelper.startCleanDB();
    instance = dbIntegrationTestHelper.createStorage(typeRegistry);
  }

  protected abstract DBIntegrationTestHelper createDBIntegrationTestHelper();

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

    assertThat(instance.getEntityOrDefaultVariation(RelationType.class, id), //
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
    RelationType storedSystemEntity = instance.getEntityOrDefaultVariation(RelationType.class, id);
    assertThat(storedSystemEntity, is(notNullValue()));

    storedSystemEntity.setRegularName(OTHER_REGULAR_NAME);

    instance.updateSystemEntity(RelationType.class, storedSystemEntity);

    assertThat(instance.getEntityOrDefaultVariation(RelationType.class, id), //
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

    List<RelationTypeMatcher> relationTypeMatchers = Lists.newArrayList(//
        matchesRelationType()//
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

    assertThat(storedSystemEntities, hasSize(3));
    assertThat(storedSystemEntities, containsInAnyOrder(relationTypeMatchers.toArray(new RelationTypeMatcher[0])));
  }

  @Test
  public void deleteSystemEntityRemovesAnEntityFromTheDatabase() throws StorageException {
    RelationType systemEntityToStore = createRelationType(REGULAR_NAME, INVERSE_NAME);
    String id = instance.addSystemEntity(RelationType.class, systemEntityToStore);
    assertThat(instance.getEntityOrDefaultVariation(RelationType.class, id), is(notNullValue()));

    instance.deleteSystemEntity(RelationType.class, id);

    assertThat(instance.getEntityOrDefaultVariation(RelationType.class, id), is(nullValue()));
  }

  /********************************************************************************
   * DomainEntity
   ********************************************************************************/

  @Test
  public void addDomainEntityAddsADomainEntityAndItsPrimitiveVersieToTheDatabase() throws Exception {
    ProjectAPerson domainEntityToStore = createPerson(GENDER, PERSON_NAME, PROJECT_A_PERSON_PROPERTY, BIRTH_DATE, DEATH_DATE);

    // action
    String id = instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntityToStore, CHANGE_TO_SAVE);

    assertThat(id, startsWith(Person.ID_PREFIX));

    List<PersonName> names = Lists.newArrayList(PERSON_NAME);
    assertThat("DomainEntity is not as expected", instance.getEntityOrDefaultVariation(DOMAIN_ENTITY_TYPE, id), //
        likeProjectAPerson() //
            .withProjectAPersonProperty(PROJECT_A_PERSON_PROPERTY) //
            .withId(id) //
            .withBirthDate(BIRTH_DATE) //
            .withDeathDate(DEATH_DATE) //
            .withGender(GENDER) //
            .withNames(names));

    assertThat("Primitive is not as expected", instance.getEntityOrDefaultVariation(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), //
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
    assertThat(instance.getEntityOrDefaultVariation(DOMAIN_ENTITY_TYPE, id), is(notNullValue()));

    // action
    instance.setPID(DOMAIN_ENTITY_TYPE, id, PID);

    ProjectAPerson updatedEntity = instance.getEntityOrDefaultVariation(DOMAIN_ENTITY_TYPE, id);
    assertThat("Entity has no pid", updatedEntity.getPid(), is(equalTo(PID)));

    int rev = updatedEntity.getRev();
    assertThat(instance.getRevision(DOMAIN_ENTITY_TYPE, id, rev), //
        likeDefaultProjectAPerson(id)//
            .withRevision(rev));
  }

  @Test
  public void updateIncreasesTheRevisionNumberAndChangesTheDomainEntityButDoesNotCreateANewVersion() throws Exception {
    String id = addDefaultProjectAPerson();

    // Store the entity
    ProjectAPerson storedDomainEntity = instance.getEntityOrDefaultVariation(DOMAIN_ENTITY_TYPE, id);
    // Make sure the entity is stored
    assertThat(storedDomainEntity, is(notNullValue()));

    int firstRevision = storedDomainEntity.getRev();

    // Update The entity
    storedDomainEntity.setBirthDate(BIRTH_DATE2);
    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, storedDomainEntity, UPDATE_CHANGE);

    ProjectAPerson updatedEntity = instance.getEntityOrDefaultVariation(DOMAIN_ENTITY_TYPE, id);

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
        instance.getEntityOrDefaultVariation(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), likeDefaultPerson(id));

    assertThat("No revision should be created for version 1",//
        instance.getRevision(DOMAIN_ENTITY_TYPE, id, firstRevision), is(nullValue()));

    int secondRevision = updatedEntity.getRev();
    assertThat("No revision should be created for version 2",//
        instance.getRevision(DOMAIN_ENTITY_TYPE, id, secondRevision), is(nullValue()));
  }

  @Ignore("Delete does not work. Test with multiple project variants.")
  @Test
  public void deletePersistentDomainEntityClearsTheEntityPropertiesSetsTheDeletedFlagToTrue() throws Exception {
    String id = addDefaultProjectAPerson();

    assertThat(instance.getEntityOrDefaultVariation(DOMAIN_ENTITY_TYPE, id), //
        likeDefaultProjectAPerson(id)//
            .withDeletedFlag(false));

    assertThat(instance.getEntityOrDefaultVariation(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), //
        likeDefaultPerson(id)//
            .withDeletedFlag(false));

    instance.deleteDomainEntity(DOMAIN_ENTITY_TYPE, id, UPDATE_CHANGE);

    int expectedRevision = 2;
    assertThat(instance.getEntityOrDefaultVariation(DOMAIN_ENTITY_TYPE, id), //
        likeDefaultProjectAPerson(id).withRevision(expectedRevision));
    assertThat(instance.getEntityOrDefaultVariation(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), //
        likeDefaultPerson(id).withRevision(expectedRevision));

  }

  @Test
  public void deleteNonPersistentDomainEntityRemovesTheCompleteDomainEntity() throws Exception {
    String id = addDefaultProjectAPerson();

    assertThat(instance.getEntityOrDefaultVariation(DOMAIN_ENTITY_TYPE, id), likeDefaultProjectAPerson(id));
    assertThat(instance.getEntityOrDefaultVariation(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), likeDefaultPerson(id));

    instance.deleteNonPersistent(DOMAIN_ENTITY_TYPE, Lists.newArrayList(id));

    assertThat(instance.getEntityOrDefaultVariation(DOMAIN_ENTITY_TYPE, id), is(nullValue()));
    assertThat(instance.getEntityOrDefaultVariation(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), is(nullValue()));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getDomainEntitiesReturnsAllDomainEntitiesOfTheRequestedType() throws Exception {
    String id1 = addPerson(GENDER, PERSON_NAME, PROJECT_A_PERSON_PROPERTY, BIRTH_DATE, DEATH_DATE);
    String id2 = addPerson(GENDER1, PERSON_NAME1, PROJECT_A_PERSON_PROPERTY1, BIRTH_DATE1, DEATH_DATE1);
    String id3 = addPerson(GENDER2, PERSON_NAME2, PROJECT_A_PERSON_PROPERTY2, BIRTH_DATE2, DEATH_DATE2);

    List<ProjectAPerson> persons = instance.getDomainEntities(DOMAIN_ENTITY_TYPE).getAll();

    PersonMatcher<? super ProjectAPerson> personMatcher = likeProjectAPerson() //
        .withProjectAPersonProperty(PROJECT_A_PERSON_PROPERTY)//
        .withBirthDate(BIRTH_DATE) //
        .withDeathDate(DEATH_DATE) //
        .withGender(GENDER) //
        .withId(id1) //
        .withNames(Lists.newArrayList(PERSON_NAME));
    PersonMatcher<? super ProjectAPerson> personMatcher1 = likeProjectAPerson() //
        .withProjectAPersonProperty(PROJECT_A_PERSON_PROPERTY1)//
        .withBirthDate(BIRTH_DATE1) //
        .withDeathDate(DEATH_DATE1) //
        .withGender(GENDER1) //
        .withId(id2) //
        .withNames(Lists.newArrayList(PERSON_NAME1));
    PersonMatcher<? super ProjectAPerson> personMatcher2 = likeProjectAPerson() //
        .withProjectAPersonProperty(PROJECT_A_PERSON_PROPERTY2)//
        .withBirthDate(BIRTH_DATE2) //
        .withDeathDate(DEATH_DATE2) //
        .withGender(GENDER2) //
        .withId(id3) //
        .withNames(Lists.newArrayList(PERSON_NAME2));

    assertThat(persons, hasSize(3));
    assertThat(persons, containsInAnyOrder(personMatcher, personMatcher1, personMatcher2));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getAllVariationsReturnsAllTheVariationsOfADomainEntity() throws Exception {
    String id = addDefaultProjectAPerson();

    List<? extends Person> allVariations = instance.getAllVariations(PRIMITIVE_DOMAIN_ENTITY_TYPE, id);

    @SuppressWarnings("rawtypes")
    Matcher[] matchers = { likeDefaultPerson(id), likeDefaultProjectAPerson(id) };

    assertThat(allVariations, hasSize(2));
    assertThat(allVariations, containsInAnyOrder(matchers));
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
    return addPerson(GENDER, PERSON_NAME, PROJECT_A_PERSON_PROPERTY, BIRTH_DATE, DEATH_DATE);
  }

  private String addPerson(Gender gender, PersonName name, String projectAPersonProperty, Datable birthDate, Datable deathDate) throws StorageException {
    ProjectAPerson domainEntityToStore = createPerson(gender, name, projectAPersonProperty, birthDate, deathDate);
    return instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntityToStore, CHANGE_TO_SAVE);
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
