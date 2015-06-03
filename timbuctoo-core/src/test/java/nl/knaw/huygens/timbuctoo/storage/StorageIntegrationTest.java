package nl.knaw.huygens.timbuctoo.storage;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static nl.knaw.huygens.timbuctoo.storage.PersonMatcher.likePerson;
import static nl.knaw.huygens.timbuctoo.storage.PersonMatcher.likeProjectAPerson;
import static nl.knaw.huygens.timbuctoo.storage.RelationMatcher.likeRelation;
import static nl.knaw.huygens.timbuctoo.storage.RelationTypeMatcher.matchesRelationType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static test.model.projecta.ProjectAPerson.PROJECT_A_PERSON_PROPERTY_NAME;

import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Person.Gender;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;
import test.model.projecta.ProjectAPerson;
import test.model.projecta.SubARelation;
import test.model.projectb.ProjectBPerson;

import com.google.common.collect.Lists;

public abstract class StorageIntegrationTest {
  private static final Class<RelationType> SYSTEM_ENTITY_TYPE = RelationType.class;
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private static final String RELATION_TARGET_TYPE = "person";
  private static final String RELATION_SOURCE_TYPE = RELATION_TARGET_TYPE;
  private static final String RELATIONTYPE_TYPE_STRING = "relationtype";
  private static final boolean NOT_ACCEPTED = false;
  private static final Class<SubARelation> PROJECT_RELATION_TYPE = RELATION_TYPE;
  private static final Class<Relation> PRIMITIVE_RELATION_TYPE = Relation.class;
  private static final boolean ACCEPTED = true;
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
  private static final String PID2 = "pid2";
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

  /* ******************************************************************************
   * SystemEntity
   * ******************************************************************************/

  @Test
  public void addSystemEntityAddsASystemEntityToTheStorageAndReturnsItsId() throws Exception {
    String id = addSystemEntity(REGULAR_NAME, INVERSE_NAME);

    assertThat(id, startsWith(RelationType.ID_PREFIX));

    assertThat(instance.getEntity(SYSTEM_ENTITY_TYPE, id), //
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
    String id = addSystemEntity(REGULAR_NAME, INVERSE_NAME);
    RelationType storedSystemEntity = instance.getEntity(SYSTEM_ENTITY_TYPE, id);
    assertThat(storedSystemEntity, is(notNullValue()));
    storedSystemEntity.setRegularName(OTHER_REGULAR_NAME);

    instance.updateSystemEntity(SYSTEM_ENTITY_TYPE, storedSystemEntity);

    assertThat(instance.getEntity(SYSTEM_ENTITY_TYPE, id), //
        matchesRelationType() //
            .withId(id)//
            .withInverseName(INVERSE_NAME)//
            .withRegularName(OTHER_REGULAR_NAME));

  }

  @Test
  public void getSystemEntitiesReturnsAllTheSystemEntitiesOfACertainType() throws Exception {
    String id1 = addSystemEntity(REGULAR_NAME, INVERSE_NAME);
    String id2 = addSystemEntity(REGULAR_NAME1, INVERSE_NAME1);
    String id3 = addSystemEntity(REGULAR_NAME2, INVERSE_NAME2);

    List<RelationType> storedSystemEntities = instance.getSystemEntities(SYSTEM_ENTITY_TYPE).getAll();

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

  private String addSystemEntity(String regularName, String inverseName) throws StorageException {
    RelationType systemEntityToStore1 = createRelationType(regularName, inverseName);
    String id1 = instance.addSystemEntity(SYSTEM_ENTITY_TYPE, systemEntityToStore1);
    return id1;
  }

  @Test
  public void deleteSystemEntityRemovesAnEntityFromTheDatabase() throws StorageException {
    // setup
    String id = addSystemEntity(REGULAR_NAME, INVERSE_NAME);
    assertThat(instance.getEntity(SYSTEM_ENTITY_TYPE, id), is(notNullValue()));

    // action
    instance.deleteSystemEntity(SYSTEM_ENTITY_TYPE, id);

    // verify
    assertThat(instance.getEntity(SYSTEM_ENTITY_TYPE, id), is(nullValue()));
  }

  @Test
  public void deleteSystemEntitiesRemovesAllSystemEntitiesOfACertainType() throws Exception {
    // setup
    String id = addSystemEntity("test", "tset");
    String id2 = addSystemEntity("test2", "2tset");

    TestSystemEntityWrapper otherTypeSystemEntity = new TestSystemEntityWrapper();
    String id3 = instance.addSystemEntity(TestSystemEntityWrapper.class, otherTypeSystemEntity);

    // action
    instance.deleteSystemEntities(SYSTEM_ENTITY_TYPE);

    // verify : check that only the SystemEntites of a certain type are removed
    assertThat(instance.getEntity(SYSTEM_ENTITY_TYPE, id), is(nullValue()));
    assertThat(instance.getEntity(SYSTEM_ENTITY_TYPE, id2), is(nullValue()));
    assertThat(instance.getEntity(TestSystemEntityWrapper.class, id3), is(not(nullValue())));
  }

  @Test
  public void deleteByModifiedRemovesTheSystemEntityThatAreLastModifiedBeforeACertainDate() throws Exception {
    // setup
    String idOfEntityBefore = addSystemEntity(REGULAR_NAME, INVERSE_NAME);
    Thread.sleep(1000); // sleep to make sure the dates are not the same
    Date deleteBefore = new Date();
    Thread.sleep(1000); // sleep to make sure the dates are not the same
    String idOfEntityAfter = addSystemEntity(REGULAR_NAME1, INVERSE_NAME1);

    // action
    instance.deleteByModifiedDate(SYSTEM_ENTITY_TYPE, deleteBefore);

    // verify
    RelationType entityCreatedBeforeDate = instance.getEntity(SYSTEM_ENTITY_TYPE, idOfEntityBefore);
    assertThat(entityCreatedBeforeDate, is(nullValue()));
    RelationType entityCreateAfterDate = instance.getEntity(SYSTEM_ENTITY_TYPE, idOfEntityAfter);
    assertThat(entityCreateAfterDate, is(not(nullValue())));
  }

  @Test
  public void entityExistsForSystemEntityShowsIfTheEntityExistsInTheDatabase() throws Exception {
    // setup
    String id = "";
    assertThat(instance.entityExists(SYSTEM_ENTITY_TYPE, id), is(false));
    id = addSystemEntity(REGULAR_NAME, INVERSE_NAME);

    // action
    boolean exists = instance.entityExists(SYSTEM_ENTITY_TYPE, id);

    // verify
    assertThat(exists, is(true));
  }

  @Test
  public void findItemByPropertyForSystemEntityReturnsTheFirstFoundInTheDatabase() throws Exception {
    String id = addSystemEntity(REGULAR_NAME, INVERSE_NAME);

    // action
    RelationType foundRelationType = instance.findItemByProperty(SYSTEM_ENTITY_TYPE, RelationType.REGULAR_NAME, REGULAR_NAME);

    // verify
    assertThat(foundRelationType, matchesRelationType()//
        .withId(id) //
        .withInverseName(INVERSE_NAME) //
        .withRegularName(REGULAR_NAME));
  }

  @Test
  public void countSystemEntityReturnsAllTheNumberOfEntitiseOfACertainType() throws Exception {
    // setup
    addSystemEntity(REGULAR_NAME, INVERSE_NAME);
    addSystemEntity(REGULAR_NAME1, INVERSE_NAME1);
    addSystemEntity(REGULAR_NAME2, INVERSE_NAME2);

    // action
    long count = instance.count(SYSTEM_ENTITY_TYPE);

    // verify
    long three = 3l;
    assertThat(count, is(equalTo(three)));
  }

  /* ******************************************************************************
   * DomainEntity
   * ******************************************************************************/

  @Test
  public void addDomainEntityAddsADomainEntityAndItsPrimitiveVersieToTheDatabase() throws Exception {
    ProjectAPerson domainEntityToStore = createProjectAPerson(GENDER, PERSON_NAME, PROJECT_A_PERSON_PROPERTY, BIRTH_DATE, DEATH_DATE);

    // action
    String id = instance.addDomainEntity(DOMAIN_ENTITY_TYPE, domainEntityToStore, CHANGE_TO_SAVE);

    assertThat(id, startsWith(Person.ID_PREFIX));

    List<PersonName> names = Lists.newArrayList(PERSON_NAME);
    assertThat("DomainEntity is not as expected", instance.getEntity(DOMAIN_ENTITY_TYPE, id), //
        likeProjectAPerson() //
            .withProjectAPersonProperty(PROJECT_A_PERSON_PROPERTY) //
            .withId(id) //
            .withBirthDate(BIRTH_DATE) //
            .withDeathDate(DEATH_DATE) //
            .withGender(GENDER) //
            .withNames(names));

    assertThat("Primitive is not as expected", instance.getEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), //
        likePerson()//
            .withId(id) //
            .withNames(names)//
            .withGender(GENDER)//
            .withBirthDate(BIRTH_DATE)//
            .withDeathDate(DEATH_DATE));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getAllRevisionsReturnsAllTheRevisionsOfADomainEntity() throws StorageException {
    // setup
    String id = addDefaultProjectAPerson();
    instance.setPID(DOMAIN_ENTITY_TYPE, id, PID);
    ProjectAPerson entity = instance.getEntity(DOMAIN_ENTITY_TYPE, id);
    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, entity, UPDATE_CHANGE);
    instance.setPID(DOMAIN_ENTITY_TYPE, id, PID2);

    // action
    List<ProjectAPerson> revisions = instance.getAllRevisions(DOMAIN_ENTITY_TYPE, id);

    // verify
    assertThat(revisions, containsInAnyOrder( //
        likeDefaultPerson(id).withRevision(1), //
        likeDefaultPerson(id).withRevision(2)));
  }

  @Test
  public void getEntityOrDefaultVariationReturnsTheEntityAndItsValuesIfItExistsElseItReturnsTheEntityWithTheValuesOfTheDefaultVariation() throws Exception {
    // setup
    String id = addDefaultProjectAPerson();

    // action
    ProjectBPerson entityWithoutVariant = instance.getEntityOrDefaultVariation(ProjectBPerson.class, id);
    Person primitive = instance.getEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, id);

    // verify 
    assertThat(entityWithoutVariant, likePerson()//
        .withBirthDate(primitive.getBirthDate())//
        .withDeathDate(primitive.getDeathDate())//
        .withGender(primitive.getGender())//
        .withId(id)//
        .withNames(primitive.getNames()));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getEntitiesByPropertyReturnsAStorageIteratorWithEntitesWithTheGivenPropertyAndValue() throws Exception {
    // setup
    String id = addPerson(GENDER, PERSON_NAME, PROJECT_A_PERSON_PROPERTY, BIRTH_DATE, DEATH_DATE);
    String id2 = addPerson(GENDER2, PERSON_NAME2, PROJECT_A_PERSON_PROPERTY, BIRTH_DATE2, DEATH_DATE2);
    addPerson(GENDER1, PERSON_NAME1, PROJECT_A_PERSON_PROPERTY1, BIRTH_DATE1, DEATH_DATE1);

    // action
    StorageIterator<ProjectAPerson> iterator = instance.getEntitiesByProperty(DOMAIN_ENTITY_TYPE, PROJECT_A_PERSON_PROPERTY_NAME, PROJECT_A_PERSON_PROPERTY);

    // verify 
    List<ProjectAPerson> all = iterator.getAll();
    assertThat(all.size(), is(2));
    assertThat(all, containsInAnyOrder(likePerson().withId(id), likePerson().withId(id2)));

  }

  @Test
  public void setPIDGivesTheDomainEntityAPidAndCreatesAVersion() throws Exception {
    String id = addDefaultProjectAPerson();
    // Make sure the entity exist
    assertThat(instance.getEntity(DOMAIN_ENTITY_TYPE, id), is(notNullValue()));

    // action
    instance.setPID(DOMAIN_ENTITY_TYPE, id, PID);

    // verify
    ProjectAPerson updatedEntity = instance.getEntity(DOMAIN_ENTITY_TYPE, id);
    assertThat("Entity has no pid", updatedEntity.getPid(), is(equalTo(PID)));

    int rev = updatedEntity.getRev();
    assertThat(instance.getRevision(DOMAIN_ENTITY_TYPE, id, rev), //
        likeDefaultProjectAPerson(id)//
            .withRevision(rev));
  }

  @Test
  public void updateDomainEntityIncreasesTheRevisionNumberAndChangesTheDomainEntityButDoesNotCreateANewVersion() throws Exception {
    String id = addDefaultProjectAPerson();

    // Store the entity
    ProjectAPerson storedDomainEntity = instance.getEntity(DOMAIN_ENTITY_TYPE, id);
    // Make sure the entity is stored
    assertThat(storedDomainEntity, is(notNullValue()));

    int firstRevision = storedDomainEntity.getRev();

    // Update The entity
    storedDomainEntity.setBirthDate(BIRTH_DATE2);
    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, storedDomainEntity, UPDATE_CHANGE);

    ProjectAPerson updatedEntity = instance.getEntity(DOMAIN_ENTITY_TYPE, id);

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
        instance.getEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), likeDefaultPerson(id));

    assertThat("No revision should be created for version 1",//
        instance.getRevision(DOMAIN_ENTITY_TYPE, id, firstRevision), is(nullValue()));

    int secondRevision = updatedEntity.getRev();
    assertThat("No revision should be created for version 2",//
        instance.getRevision(DOMAIN_ENTITY_TYPE, id, secondRevision), is(nullValue()));
  }

  // FIXME this is a hack with Mongo update and should eventually be extracted to a different method.
  // see TIM-156
  @Test
  public void updateDomainEntityWithADifferentTypeAddsTheNewFields() throws Exception {
    // setup
    String id = addDefaultProjectAPerson();

    ProjectAPerson projectAPerson = instance.getEntity(ProjectAPerson.class, id);

    ProjectBPerson projectBPerson = new ProjectBPerson();
    projectBPerson.addName(PERSON_NAME2);
    projectBPerson.setBirthDate(BIRTH_DATE2);
    projectBPerson.setId(projectAPerson.getId());
    projectBPerson.setRev(projectAPerson.getRev());

    // action
    instance.updateDomainEntity(ProjectBPerson.class, projectBPerson, UPDATE_CHANGE);

    // verify
    ProjectBPerson foundProjectBPerson = instance.getEntity(ProjectBPerson.class, id);

    assertThat(foundProjectBPerson.getNames(), contains(PERSON_NAME2));
    assertThat(foundProjectBPerson.getBirthDate(), is(BIRTH_DATE2));

  }

  @Test
  public void updateDomainEntityRemovesThePIDIfTheEntityHasOne() throws Exception {
    String id = addDefaultProjectAPerson();

    // Store the entity
    ProjectAPerson storedDomainEntity = instance.getEntity(DOMAIN_ENTITY_TYPE, id);
    // Make sure the entity is stored
    assertThat(storedDomainEntity, is(notNullValue()));

    // set the pid
    instance.setPID(DOMAIN_ENTITY_TYPE, id, PID);

    // check if the entity has a PID
    ProjectAPerson entity = instance.getEntity(DOMAIN_ENTITY_TYPE, id);
    assertThat("Entity has no PID", entity.getPid(), is(PID));

    //  Update The entity
    storedDomainEntity.setBirthDate(BIRTH_DATE2);
    instance.updateDomainEntity(DOMAIN_ENTITY_TYPE, storedDomainEntity, UPDATE_CHANGE);

    ProjectAPerson updatedEntity = instance.getEntity(DOMAIN_ENTITY_TYPE, id);

    // check if the PID is removed 
    assertThat(updatedEntity.getPid(), is(nullValue()));
  }

  @Test
  public void deleteDomainEntityRemovesTheEntityFromTheDatabase() throws Exception {
    String id = addDefaultProjectAPerson();

    assertThat(instance.getEntity(DOMAIN_ENTITY_TYPE, id), //
        likeDefaultProjectAPerson(id)//
            .withDeletedFlag(false));

    assertThat(instance.getEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), //
        likeDefaultPerson(id)//
            .withDeletedFlag(false));

    instance.deleteDomainEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, id, UPDATE_CHANGE);
    assertThat(instance.getEntity(DOMAIN_ENTITY_TYPE, id), is(nullValue()));
    assertThat(instance.getEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), is(nullValue()));
  }

  @Test
  public void deleteVariationRemovesTheVariationFromTheEntity() throws Exception {
    String id = addDefaultProjectAPerson();

    assertThat(instance.getEntity(DOMAIN_ENTITY_TYPE, id), //
        likeDefaultProjectAPerson(id)//
            .withDeletedFlag(false));

    assertThat(instance.getEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), //
        likeDefaultPerson(id)//
            .withDeletedFlag(false));

    // action
    instance.deleteVariation(DOMAIN_ENTITY_TYPE, id, UPDATE_CHANGE);

    int expectedRevision = 2;
    assertThat(instance.getEntity(DOMAIN_ENTITY_TYPE, id), is(nullValue()));
    assertThat(instance.getEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), //
        likeDefaultPerson(id).withRevision(expectedRevision));
  }

  @Test
  public void deleteVariationRemovesThePIDWhenTheEntityHasOne() throws Exception {
    // setup
    String id = addDefaultProjectAPerson();

    // set PID and check if the PID is set
    instance.setPID(DOMAIN_ENTITY_TYPE, id, PID);
    ProjectAPerson entity = instance.getEntity(DOMAIN_ENTITY_TYPE, id);
    assertThat(entity, likeDefaultProjectAPerson(id).withPID());

    // action
    instance.deleteVariation(DOMAIN_ENTITY_TYPE, id, UPDATE_CHANGE);

    // verify
    Person updatedEntity = instance.getEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, id);
    assertThat(updatedEntity, likeDefaultPerson(id).withoutPID());

  }

  @Test
  public void declineRelationsOfEntitySetsAcceptedToFalseForTheVariation() throws Exception {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();

    String relationId = addDefaultRelation(sourceId, targetId, typeId);

    // check if the relation is added
    assertThat(instance.getEntity(PROJECT_RELATION_TYPE, relationId), likeDefaultAcceptedRelation(sourceId, targetId, typeId));
    assertThat(instance.getEntity(PRIMITIVE_RELATION_TYPE, relationId), likeDefaultAcceptedRelation(sourceId, targetId, typeId));

    // action
    instance.declineRelationsOfEntity(PROJECT_RELATION_TYPE, sourceId);

    // verify
    assertThat(instance.getEntity(PROJECT_RELATION_TYPE, relationId), likeDefaultNotAcceptionRelation(sourceId, targetId, typeId));
    assertThat(instance.getEntity(PRIMITIVE_RELATION_TYPE, relationId), likeDefaultAcceptedRelation(sourceId, targetId, typeId));
  }

  @Test
  public void declineRelationsOfEntityRemovesThePIDOfTheRelationsIfTheyHaveOne() throws Exception {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();

    String id = addDefaultRelation(sourceId, targetId, typeId);

    // set the PID and check if it's added
    instance.setPID(RELATION_TYPE, id, PID);
    SubARelation relation = instance.getEntity(RELATION_TYPE, id);
    assertThat(relation, //
        likeDefaultAcceptedRelation(sourceId, targetId, typeId) //
            .withPID());

    // action
    instance.declineRelationsOfEntity(PROJECT_RELATION_TYPE, sourceId);

    // verify
    SubARelation declinedRelation = instance.getEntity(RELATION_TYPE, id);
    assertThat(declinedRelation, //
        likeDefaultNotAcceptionRelation(sourceId, targetId, typeId) //
            .withoutPID());
  }

  @Test
  public void deleteRelationsOfEntityRemovesAllTheRelationsConnectedToTheEntity() throws Exception {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();

    String relationId = addDefaultRelation(sourceId, targetId, typeId);

    // check if the relation is added
    assertThat(instance.getEntity(PROJECT_RELATION_TYPE, relationId), likeDefaultAcceptedRelation(sourceId, targetId, typeId));
    assertThat(instance.getEntity(PRIMITIVE_RELATION_TYPE, relationId), likeDefaultAcceptedRelation(sourceId, targetId, typeId));

    // action
    instance.deleteRelationsOfEntity(PRIMITIVE_RELATION_TYPE, sourceId);

    // verify
    assertThat(instance.getEntity(PROJECT_RELATION_TYPE, relationId), is(nullValue()));
    assertThat(instance.getEntity(PRIMITIVE_RELATION_TYPE, relationId), is(nullValue()));
  }

  @Test
  public void deleteNonPersistentDomainEntityRemovesTheCompleteDomainEntity() throws Exception {
    String id = addDefaultProjectAPerson();

    assertThat(instance.getEntity(DOMAIN_ENTITY_TYPE, id), likeDefaultProjectAPerson(id));
    assertThat(instance.getEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), likeDefaultPerson(id));

    instance.deleteNonPersistent(DOMAIN_ENTITY_TYPE, Lists.newArrayList(id));

    assertThat(instance.getEntity(DOMAIN_ENTITY_TYPE, id), is(nullValue()));
    assertThat(instance.getEntity(PRIMITIVE_DOMAIN_ENTITY_TYPE, id), is(nullValue()));
  }

  @Test
  public void entityExistsForDomainEntityShowsIfTheEntityExistsInTheDatabase() throws Exception {
    // setup
    String id = "";
    assertThat(instance.entityExists(DOMAIN_ENTITY_TYPE, id), is(false));
    id = addDefaultProjectAPerson();

    // action
    boolean exists = instance.entityExists(DOMAIN_ENTITY_TYPE, id);

    // verify
    assertThat(exists, is(true));
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

  @Test
  public void getAllVariationsReturnsAllTheVariationsOfADomainEntity() throws Exception {
    String id = addDefaultProjectAPerson();

    List<Person> allVariations = instance.getAllVariations(PRIMITIVE_DOMAIN_ENTITY_TYPE, id);

    assertThat(allVariations, hasSize(2));
    // needed to do this, to be able to check if all the properties contain the right values.
    for (Person person : allVariations) {
      if (person instanceof ProjectAPerson) {
        assertThat((ProjectAPerson) person, is(likeDefaultProjectAPerson(id)));
      } else {
        assertThat(person, is(likeDefaultPerson(id)));
      }
    }
  }

  @Test
  public void findItemByPropertyForDomainEntityReturnsTheFirstDomainEntityFound() throws StorageException {
    // setup
    ProjectAPerson person = createProjectAPerson(GENDER, PERSON_NAME, PROJECT_A_PERSON_PROPERTY, BIRTH_DATE, DEATH_DATE);
    String id = instance.addDomainEntity(ProjectAPerson.class, person, CHANGE_TO_SAVE);

    // action
    ProjectAPerson foundPerson = instance.findItemByProperty(ProjectAPerson.class, ProjectAPerson.PROJECT_A_PERSON_PROPERTY_NAME, PROJECT_A_PERSON_PROPERTY);

    assertThat(foundPerson, likePerson()//
        .withBirthDate(BIRTH_DATE)//
        .withDeathDate(DEATH_DATE)//
        .withGender(GENDER)//
        .withId(id)//
        .withNames(Lists.newArrayList(PERSON_NAME)));
  }

  @Test
  public void countDomainEntityOnlyCountsTheLatestVersions() throws Exception {
    // setup
    // save person
    String id = addDefaultProjectAPerson();
    // create a new version of person.
    ProjectAPerson person = instance.getEntity(ProjectAPerson.class, id);
    person.setGender(GENDER1);
    instance.updateDomainEntity(ProjectAPerson.class, person, UPDATE_CHANGE);
    instance.setPID(ProjectAPerson.class, id, PID);

    // action
    long count = instance.count(ProjectAPerson.class);

    // verify
    long one = 1l;
    assertThat(count, is(equalTo(one)));
  }

  @Test
  public void countDomainEntityReturnsTheCountOfThePrimitive() throws Exception {
    // setup
    addDefaultProjectBPerson();
    addDefaultProjectAPerson();

    // action
    long projectACount = instance.count(ProjectAPerson.class);
    long projectBCount = instance.count(ProjectBPerson.class);
    long primitiveCount = instance.count(Person.class);

    // verify
    long two = 2l;
    assertThat(projectACount, is(equalTo(two)));
    assertThat(projectBCount, is(equalTo(two)));
    assertThat(primitiveCount, is(equalTo(two)));
  }

  // Person test helpers

  private ProjectAPerson createProjectAPerson(Gender gender, PersonName name, String projectAPersonProperty, Datable birthDate, Datable deathDate) {
    ProjectAPerson person = new ProjectAPerson();
    person.setGender(gender);
    person.addName(name);
    person.setProjectAPersonProperty(projectAPersonProperty);
    person.setBirthDate(birthDate);
    person.setDeathDate(deathDate);
    person.setTypes(Lists.newArrayList("Test"));

    return person;
  }

  private ProjectBPerson createProjectBPerson(Gender gender, PersonName name, Datable birthDate, Datable deathDate) {
    ProjectBPerson person = new ProjectBPerson();
    person.setGender(gender);
    person.addName(name);
    person.setBirthDate(birthDate);
    person.setDeathDate(deathDate);
    person.setTypes(Lists.newArrayList("Test"));

    return person;
  }

  private void addDefaultProjectBPerson() throws StorageException {
    ProjectBPerson entity = createProjectBPerson(GENDER, PERSON_NAME, BIRTH_DATE, DEATH_DATE);
    instance.addDomainEntity(ProjectBPerson.class, entity, CHANGE_TO_SAVE);
  }

  private String addDefaultProjectAPerson() throws StorageException {
    return addPerson(GENDER, PERSON_NAME, PROJECT_A_PERSON_PROPERTY, BIRTH_DATE, DEATH_DATE);
  }

  private String addPerson(Gender gender, PersonName name, String projectAPersonProperty, Datable birthDate, Datable deathDate) throws StorageException {
    ProjectAPerson domainEntityToStore = createProjectAPerson(gender, name, projectAPersonProperty, birthDate, deathDate);
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

  @Test
  public void getAllIdsWithoutPIDForDomainEntityReturnsTheNonPersistentDomainEntities() throws Exception {
    // setup
    String id = addDefaultProjectAPerson();
    instance.setPID(DOMAIN_ENTITY_TYPE, id, PID);

    String id2 = addDefaultProjectAPerson();

    // action
    List<String> idsOfNonPersistentEntities = instance.getAllIdsWithoutPIDOfType(DOMAIN_ENTITY_TYPE);

    // verify
    assertThat(idsOfNonPersistentEntities, hasItem(id2));
    assertThat(idsOfNonPersistentEntities, not(hasItem(id)));
  }

  /* *******************************************************************************
   * Relation
   * ******************************************************************************/
  @Test
  public void addRelationAddsARelationAndItsPrimitiveVersionToTheDatabase() throws Exception {
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();

    String id = addDefaultRelation(sourceId, targetId, typeId);

    // check if the relation is added
    assertThat(instance.getEntity(PROJECT_RELATION_TYPE, id), likeDefaultRelation(sourceId, targetId, typeId));
    assertThat(instance.getEntity(PRIMITIVE_RELATION_TYPE, id), likeDefaultPrimitiveRelation(id, sourceId, targetId, typeId));
  }

  private RelationMatcher likeDefaultPrimitiveRelation(String id, String sourceId, String targetId, String typeId) {
    return likeRelation()//
        .withType(PRIMITIVE_RELATION_TYPE) //
        .withId(id) //
        .withSourceId(sourceId) //
        .withSourceType(RELATION_SOURCE_TYPE) //
        .withTargetId(targetId) //
        .withTargetType(RELATION_TARGET_TYPE) //
        .withTypeId(typeId) //
        .isAccepted(ACCEPTED);
  }

  private RelationMatcher likeDefaultRelation(String sourceId, String targetId, String typeId) {
    return likeRelation() //
        .withType(RELATION_TYPE) //
        .withSourceId(sourceId) //
        .withSourceType(RELATION_SOURCE_TYPE) //
        .withTargetId(targetId) //
        .withTargetType(RELATION_TARGET_TYPE) //
        .withTypeId(typeId) //
        .isAccepted(ACCEPTED);
  }

  private String addDefaultRelation(String sourceId, String targetId, String typeId) throws StorageException {
    SubARelation relation = new SubARelation();
    relation.setAccepted(ACCEPTED);
    relation.setSourceId(sourceId);
    relation.setSourceType(RELATION_SOURCE_TYPE);
    relation.setTargetId(targetId);
    relation.setTargetType(RELATION_TARGET_TYPE);
    relation.setTypeId(typeId);
    relation.setTypeType(RELATIONTYPE_TYPE_STRING);

    String id = instance.addDomainEntity(PROJECT_RELATION_TYPE, relation, CHANGE_TO_SAVE);
    return id;
  }

  private String addRelationType() throws StorageException {
    String typeId = addSystemEntity(REGULAR_NAME, INVERSE_NAME);
    return typeId;
  }

  @Test
  public void updateRelationUpdatesTheValuesOfTheRelationAndIncreasesTheRevButDoesNotCreateANewRevision() throws Exception {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();

    String id = addDefaultRelation(sourceId, targetId, typeId);
    SubARelation relation = instance.getEntity(PROJECT_RELATION_TYPE, id);
    // assert the relation is stored
    assertThat(relation, is(notNullValue()));

    int firstRevision = relation.getRev();

    relation.setAccepted(NOT_ACCEPTED);

    // action
    instance.updateDomainEntity(PROJECT_RELATION_TYPE, relation, UPDATE_CHANGE);

    // verify
    SubARelation updateRelation = instance.getEntity(PROJECT_RELATION_TYPE, id);

    assertThat("Relation is not updated", updateRelation, //
        is(likeRelation()//
            .withId(id) //
            .withSourceId(sourceId) //
            .withSourceType(RELATION_SOURCE_TYPE) //
            .withTargetId(targetId) //
            .withTargetType(RELATION_TARGET_TYPE) //
            .withTypeId(typeId) //
            .isAccepted(NOT_ACCEPTED) //
            .withRevision(firstRevision + 1)));

    assertThat("Primitive domain entity should not have changed", //
        instance.getEntity(PRIMITIVE_RELATION_TYPE, id), likeDefaultPrimitiveRelation(id, sourceId, targetId, typeId));

    assertThat("No revision should be created for version 1",//
        instance.getRevision(DOMAIN_ENTITY_TYPE, id, firstRevision), is(nullValue()));

    int secondRevision = updateRelation.getRev();
    assertThat("No revision should be created for version 2",//
        instance.getRevision(DOMAIN_ENTITY_TYPE, id, secondRevision), is(nullValue()));
  }

  @Test
  public void updateRelationRemovesThePIDWhenItHasOne() throws Exception {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();

    String id = addDefaultRelation(sourceId, targetId, typeId);

    // set PID and check if the PID is really set
    instance.setPID(RELATION_TYPE, id, PID);
    SubARelation entity = instance.getEntity(RELATION_TYPE, id);
    assertThat(entity, //
        likeDefaultAcceptedRelation(sourceId, targetId, typeId) //
            .withPID());

    // action
    instance.updateDomainEntity(RELATION_TYPE, entity, UPDATE_CHANGE);

    // verify
    SubARelation updatedEntity = instance.getEntity(RELATION_TYPE, id);
    assertThat(updatedEntity, //
        likeDefaultAcceptedRelation(sourceId, targetId, typeId) //
            .withoutPID());
  }

  @Test
  public void setPIDForRelationCreatesANewRevisionAndFillsThePID() throws Exception {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();

    String id = addDefaultRelation(sourceId, targetId, typeId);

    // action
    instance.setPID(PROJECT_RELATION_TYPE, id, PID);

    // verify
    SubARelation updatedEntity = instance.getEntity(PROJECT_RELATION_TYPE, id);
    assertThat("Entity has no pid", updatedEntity.getPid(), is(equalTo(PID)));

    int rev = updatedEntity.getRev();
    assertThat(instance.getRevision(PROJECT_RELATION_TYPE, id, rev), //
        likeDefaultRelation(sourceId, targetId, typeId)//
            .withId(id)//
            .withRevision(rev));
  }

  @Test
  public void findItemByPropertyForRelationReturnsTheFirstRelationFound() throws Exception {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();

    addDefaultRelation(sourceId, targetId, typeId);

    // action
    SubARelation foundRelation = instance.findItemByProperty(PROJECT_RELATION_TYPE, Relation.SOURCE_ID, sourceId);

    // verify
    assertThat(foundRelation, likeRelation()//
        .withSourceId(sourceId) //
        .withSourceType(RELATION_SOURCE_TYPE) //
        .withTargetId(targetId) //
        .withTargetType(RELATION_TARGET_TYPE) //
        .withTypeId(typeId) //
        .isAccepted(ACCEPTED));

  }

  @Test
  public void countRelationsOnlyCountsTheLatest() throws Exception {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();

    String id = addDefaultRelation(sourceId, targetId, typeId);

    SubARelation relation = instance.getEntity(RELATION_TYPE, id);
    relation.setAccepted(NOT_ACCEPTED);
    instance.updateDomainEntity(RELATION_TYPE, relation, UPDATE_CHANGE);
    instance.setPID(RELATION_TYPE, id, PID);

    // action
    long count = instance.count(RELATION_TYPE);

    // verify
    long one = 1l;
    assertThat(count, is(equalTo(one)));

  }

  //Relation test helpers

  private RelationMatcher likeDefaultNotAcceptionRelation(String sourceId, String targetId, String typeId) {
    return likeRelation()//
        .withSourceId(sourceId) //
        .withSourceType(RELATION_SOURCE_TYPE) //
        .withTargetId(targetId) //
        .withTargetType(RELATION_TARGET_TYPE) //
        .withTypeId(typeId) //
        .isAccepted(NOT_ACCEPTED);
  }

  private RelationMatcher likeDefaultAcceptedRelation(String sourceId, String targetId, String typeId) {
    return likeRelation()//
        .withSourceId(sourceId) //
        .withSourceType(RELATION_SOURCE_TYPE) //
        .withTargetId(targetId) //
        .withTargetType(RELATION_TARGET_TYPE) //
        .withTypeId(typeId) //
        .isAccepted(ACCEPTED);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getRelationsByEntityIdReturnsAllTheIncomingAndOutgoingRelationsOfAnEntity() throws Exception {
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();
    String otherTypeId = addRelationType();

    String id1 = addDefaultRelation(sourceId, targetId, typeId);
    String id2 = addDefaultRelation(targetId, sourceId, otherTypeId);

    // action
    StorageIterator<Relation> relations = instance.getRelationsByEntityId(PRIMITIVE_RELATION_TYPE, sourceId);

    // verify
    assertThat(relations.getAll(), containsInAnyOrder( //
        likeRelation()//
            .withId(id1) //
            .withSourceId(sourceId) //
            .withSourceType(RELATION_SOURCE_TYPE) //
            .withTargetId(targetId) //
            .withTargetType(RELATION_TARGET_TYPE) //
            .withTypeId(typeId) //
            .isAccepted(ACCEPTED), //
        likeRelation() //
            .withId(id2) //
            .withSourceId(targetId) //
            .withSourceType(RELATION_TARGET_TYPE) //
            .withTargetId(sourceId) //
            .withTargetType(RELATION_SOURCE_TYPE) //
            .withTypeId(otherTypeId) //
            .isAccepted(ACCEPTED)));
  }

  @Test
  public void getRelationsByEntityIdReturnsTheLatestRelationsOfAnEntity() throws Exception {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();

    String id = addDefaultRelation(sourceId, targetId, typeId);

    // create a duplicate
    instance.setPID(RELATION_TYPE, id, PID);

    // action
    StorageIterator<Relation> relations = instance.getRelationsByEntityId(PRIMITIVE_RELATION_TYPE, sourceId);

    // verify
    List<Relation> allRelations = relations.getAll();
    assertThat(allRelations.size(), is(1));
    assertThat(allRelations, contains( //
        likeRelation()//
            .withId(id) //
            .withSourceId(sourceId) //
            .withSourceType(RELATION_SOURCE_TYPE) //
            .withTargetId(targetId) //
            .withTargetType(RELATION_TARGET_TYPE) //
            .withTypeId(typeId) //
            .isAccepted(ACCEPTED)));
  }

  @Test
  public void getRelationIdsReturnsAListOfRelationIdsThatBelongToTheEntityIds() throws Exception {
    // setup
    String entityId1 = addDefaultProjectAPerson();
    String entityId2 = addDefaultProjectAPerson();
    String entityId3 = addDefaultProjectAPerson();
    String entityId4 = addDefaultProjectAPerson();
    String typeId = addRelationType();

    String relBetweenE1AndE2 = addDefaultRelation(entityId1, entityId2, typeId);
    String relBetweenE2AndE3 = addDefaultRelation(entityId2, entityId3, typeId);
    String relBetweenE3AndE4 = addDefaultRelation(entityId3, entityId4, typeId);

    // action
    List<String> foundIds = instance.getRelationIds(Lists.newArrayList(entityId1, entityId2));

    // verify
    assertThat(foundIds, containsInAnyOrder(relBetweenE1AndE2, relBetweenE2AndE3));
    assertThat(foundIds, not(hasItem(relBetweenE3AndE4)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getAllRevisionsForRelationReturnsAllTheRevisionsOfARelation() throws StorageException {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();

    String id = addDefaultRelation(sourceId, targetId, typeId);
    instance.setPID(RELATION_TYPE, id, PID);
    instance.declineRelationsOfEntity(RELATION_TYPE, sourceId);
    instance.setPID(RELATION_TYPE, id, PID2);

    // action
    List<SubARelation> revisions = instance.getAllRevisions(RELATION_TYPE, id);

    // verify
    assertThat(revisions, containsInAnyOrder(//
        likeDefaultAcceptedRelation(sourceId, targetId, typeId).withRevision(1), //
        likeDefaultNotAcceptionRelation(sourceId, targetId, typeId).withRevision(2)));
  }

  @Test
  public void entityExistsForRelationShowsIfTheEntityExistsInTheDatabase() throws Exception {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();

    String id = addDefaultRelation(sourceId, targetId, typeId);

    // action
    boolean exists = instance.entityExists(RELATION_TYPE, id);

    // verify
    assertThat(exists, is(true));
  }

  @Test
  public void findRelationSearchesARelationByClassSourceIdTargetIdAndTypeId() throws Exception {
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String relationTypeId = addRelationType();

    String id = addDefaultRelation(sourceId, targetId, relationTypeId);

    // action
    SubARelation foundRelation = instance.findRelation(RELATION_TYPE, sourceId, targetId, relationTypeId);

    // verify
    assertThat(foundRelation, likeRelation() //
        .withId(id) //
        .withSourceId(sourceId) //
        .withTargetId(targetId) //
        .withTypeId(relationTypeId));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void findRelationsReturnsAnIteratorOfAllTheRelationsOfACertainTypeBetweenTwoEntities() throws Exception {
    // setup
    String entityId1 = addDefaultProjectAPerson();
    String entityId2 = addDefaultProjectAPerson();
    String entityId3 = addDefaultProjectAPerson();
    String typeId = addRelationType();
    String typeId2 = addRelationType();

    String relId1 = addDefaultRelation(entityId1, entityId2, typeId);
    String relId2 = addDefaultRelation(entityId1, entityId3, typeId);
    String relId3 = addDefaultRelation(entityId1, entityId2, typeId2);
    String relId4 = addDefaultRelation(entityId2, entityId3, typeId);

    // action
    StorageIterator<SubARelation> relations = instance.findRelations(RELATION_TYPE, entityId1, entityId2, typeId);

    // verify
    List<SubARelation> relationList = Lists.newArrayList(relations);
    assertThat(relationList, contains(likeRelation().withId(relId1)));
    assertThat(relationList, not(containsInAnyOrder( //
        likeRelation().withId(relId2), //
        likeRelation().withId(relId3), //
        likeRelation().withId(relId4))));

  }

  @SuppressWarnings("unchecked")
  @Test
  public void findRelationsReturnsAnIteratorOfAllTheRelationsOfACertainTypeForTheSourceIfTheTargetIsNull() throws Exception {
    // setup
    String entityId1 = addDefaultProjectAPerson();
    String entityId2 = addDefaultProjectAPerson();
    String entityId3 = addDefaultProjectAPerson();
    String typeId = addRelationType();
    String typeId2 = addRelationType();

    String relId1 = addDefaultRelation(entityId1, entityId2, typeId);
    String relId2 = addDefaultRelation(entityId1, entityId3, typeId);
    String relId3 = addDefaultRelation(entityId1, entityId2, typeId2);
    String relId4 = addDefaultRelation(entityId2, entityId3, typeId);

    // action
    StorageIterator<SubARelation> relations = instance.findRelations(RELATION_TYPE, entityId2, null, typeId);

    // verify
    List<SubARelation> relationList = Lists.newArrayList(relations);
    assertThat(relationList, contains(likeRelation().withId(relId4)));
    assertThat(relationList, not(containsInAnyOrder( //
        likeRelation().withId(relId2), //
        likeRelation().withId(relId3), //
        likeRelation().withId(relId1))));

  }

  @SuppressWarnings("unchecked")
  @Test
  public void findRelationsReturnsAnIteratorOfAllTheRelationsOfACertainTypeForTheTargetIfTheSourceIsNull() throws Exception {
    // setup
    String entityId1 = addDefaultProjectAPerson();
    String entityId2 = addDefaultProjectAPerson();
    String entityId3 = addDefaultProjectAPerson();
    String typeId = addRelationType();
    String typeId2 = addRelationType();

    String relId1 = addDefaultRelation(entityId1, entityId2, typeId);
    String relId2 = addDefaultRelation(entityId1, entityId3, typeId);
    String relId3 = addDefaultRelation(entityId1, entityId2, typeId2);
    String relId4 = addDefaultRelation(entityId2, entityId3, typeId);

    // action
    StorageIterator<SubARelation> relations = instance.findRelations(RELATION_TYPE, null, entityId3, typeId);

    // verify
    List<SubARelation> relationList = Lists.newArrayList(relations);
    assertThat(relationList, containsInAnyOrder( //
        likeRelation().withId(relId2), //
        likeRelation().withId(relId4)));
    assertThat(relationList, not(containsInAnyOrder( //
        likeRelation().withId(relId1), //
        likeRelation().withId(relId3))));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void findRelationsReturnsAnIteratorOfAllTheRelationsOfBetweenTheSourceAndTargetIfTheTypeIsNull() throws Exception {
    // setup
    String entityId1 = addDefaultProjectAPerson();
    String entityId2 = addDefaultProjectAPerson();
    String entityId3 = addDefaultProjectAPerson();
    String typeId = addRelationType();
    String typeId2 = addRelationType();

    String relId1 = addDefaultRelation(entityId1, entityId2, typeId);
    String relId2 = addDefaultRelation(entityId1, entityId3, typeId);
    String relId3 = addDefaultRelation(entityId1, entityId2, typeId2);
    String relId4 = addDefaultRelation(entityId2, entityId3, typeId);

    // action
    StorageIterator<SubARelation> relations = instance.findRelations(RELATION_TYPE, entityId1, entityId2, null);

    // verify
    List<SubARelation> relationList = Lists.newArrayList(relations);
    assertThat(relationList, containsInAnyOrder( //
        likeRelation().withId(relId1), //
        likeRelation().withId(relId3)));
    assertThat(relationList, not(containsInAnyOrder( //
        likeRelation().withId(relId2), //
        likeRelation().withId(relId4))));
  }

  @Test
  public void getAllIdsWithoutPIDForRelationReturnsTheIdsOfNonPersistentDomainEntities() throws Exception {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();
    String id = addDefaultRelation(sourceId, targetId, typeId);
    instance.setPID(PROJECT_RELATION_TYPE, id, PID);

    String id2 = addDefaultRelation(sourceId, targetId, typeId);

    // action
    List<String> idsOfNonRelations = instance.getAllIdsWithoutPIDOfType(PROJECT_RELATION_TYPE);

    // verify
    assertThat(idsOfNonRelations, hasItem(id2));
    assertThat(idsOfNonRelations, not(hasItem(id)));
  }

  @Test
  public void getAllVariationsForRelationsReturnsAllTheVariationsOfARelation() throws Exception {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();
    String id = addDefaultRelation(sourceId, targetId, typeId);

    // action
    List<Relation> allVariations = instance.getAllVariations(PRIMITIVE_RELATION_TYPE, id);

    // verify
    assertThat(allVariations.size(), is(2));
    assertThat(allVariations, hasItem(likeDefaultAcceptedRelation(sourceId, targetId, typeId)));
    assertThat(allVariations, hasItem(likeDefaultPrimitiveRelation(id, sourceId, targetId, typeId)));
  }

  @Test
  public void deleteDomainEntityForRelationsRemovesTheRelationsFromTheDatabase() throws Exception {
    // setup
    String sourceId = addDefaultProjectAPerson();
    String targetId = addDefaultProjectAPerson();
    String typeId = addRelationType();
    String id = addDefaultRelation(sourceId, targetId, typeId);

    // action
    instance.deleteDomainEntity(PRIMITIVE_RELATION_TYPE, id, UPDATE_CHANGE);

    // verify
    assertThat(instance.getEntity(RELATION_TYPE, id), is(nullValue()));

  }

  /* **************************************************************************
   * Other methods
   * **************************************************************************/
  @Test
  public void closeClosesTheDatabaseconnection() {
    assertThat("Storage is not available before closing", instance.isAvailable(), is(equalTo(true)));

    instance.close();

    assertThat("Storage is not closed", instance.isAvailable(), is(equalTo(false)));
  }
}
