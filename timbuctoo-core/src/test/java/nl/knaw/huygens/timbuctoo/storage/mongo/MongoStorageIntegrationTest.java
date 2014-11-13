package nl.knaw.huygens.timbuctoo.storage.mongo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;

import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;

import test.variation.model.BaseVariationDomainEntity;
import test.variation.model.TestSystemEntity;
import test.variation.model.projecta.ProjectADomainEntity;

public class MongoStorageIntegrationTest extends DBIntegrationTest {
  private static final Class<TestSystemEntity> SYSTEM_TYPE = TestSystemEntity.class;
  static final String GENERAL_STRING_VALUE = "test";
  private static final Class<ProjectADomainEntity> DOMAIN_TYPE = ProjectADomainEntity.class;
  private static final Change DEFAULT_CHANGE = new Change();
  MongoStorage instance;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    instance = createMongoStorage(TypeRegistry.getInstance());
  }

  /**************************************************************************************
   * DomainEntity 
   **************************************************************************************/

  @Test
  public void addDomainEntityShouldAddAnItemToTheDatabase() throws StorageException {
    // action
    String id = addProjectADomainEntityToDatabase(GENERAL_STRING_VALUE);

    // verify
    BaseVariationDomainEntity foundItem = instance.getItem(BaseVariationDomainEntity.class, id);
    assertThat(foundItem, is(notNullValue(BaseVariationDomainEntity.class)));
    assertThat(foundItem.generalTestDocValue, is(equalTo(GENERAL_STRING_VALUE)));
  }

  @Test
  public void addDomainEntityShouldAddPrimitiveVariationTheDatabase() throws StorageException {
    // action
    String id = addProjectADomainEntityToDatabase(GENERAL_STRING_VALUE);

    // verify
    ProjectADomainEntity foundItem = instance.getItem(DOMAIN_TYPE, id);
    assertThat(foundItem, isNotNullProjectADomainEntity());
    assertThat(foundItem.generalTestDocValue, is(equalTo(GENERAL_STRING_VALUE)));
  }

  @Test
  public void updateDomainEntityShouldUpdateTheItemAndIncreaseTheRevision() throws UpdateException, StorageException {
    // setup
    String id = addProjectADomainEntityToDatabase(GENERAL_STRING_VALUE);
    String otherGeneralTestDocValue = "otherGeneralTestDocValue";

    ProjectADomainEntity entityToUpdate = instance.getItem(DOMAIN_TYPE, id);
    entityToUpdate.generalTestDocValue = otherGeneralTestDocValue;

    // action
    instance.updateDomainEntity(DOMAIN_TYPE, entityToUpdate, DEFAULT_CHANGE);

    // verify
    ProjectADomainEntity updatedEntity = instance.getItem(DOMAIN_TYPE, id);
    assertThat(updatedEntity, isNotNullProjectADomainEntity());
    assertThat(updatedEntity.generalTestDocValue, is(equalTo(otherGeneralTestDocValue)));
    assertThat(updatedEntity.getRev(), is(equalTo(2)));
  }

  @Test
  public void getDomainEntitiesReturnsAnIteratorForAllDomainEntitiesInTheDatabase() throws StorageException {
    // setup
    int numberOfEntities = 3;
    for (int i = 0; i < numberOfEntities; i++) {
      addProjectADomainEntityToDatabase(GENERAL_STRING_VALUE);
    }

    // action
    StorageIterator<ProjectADomainEntity> allProjectADomainEntities = instance.getDomainEntities(DOMAIN_TYPE);

    // verify
    assertThat(allProjectADomainEntities.size(), is(equalTo(numberOfEntities)));
  }

  @Ignore("The concept of deleting domain entities should still be discussed")
  @Test
  public void deleteDomainEntity() {
    fail("Yet to be implemented.");
  }

  @Test
  public void setPIDFillsThePIDpropertyAndAddsTheVersionToVersionCollection() throws StorageException {
    // setup
    String id = addProjectADomainEntityToDatabase(GENERAL_STRING_VALUE);

    // action
    String pid = "randomPID";
    instance.setPID(DOMAIN_TYPE, id, pid);

    // verify
    ProjectADomainEntity foundItem = instance.getItem(DOMAIN_TYPE, id);
    assertThat(foundItem, isNotNullProjectADomainEntity());
    assertThat(foundItem.getPid(), is(notNullValue(String.class)));

    assertThat(instance.getRevision(ProjectADomainEntity.class, id, 1), isNotNullProjectADomainEntity());
  }

  private String addProjectADomainEntityToDatabase(String generalTestDocValue) throws StorageException {
    ProjectADomainEntity entity = createEntityWithGeneralTestDocValue(generalTestDocValue);
    String id = instance.addDomainEntity(DOMAIN_TYPE, entity, DEFAULT_CHANGE);
    return id;
  }

  private Matcher<ProjectADomainEntity> isNotNullProjectADomainEntity() {
    return is(notNullValue(ProjectADomainEntity.class));
  }

  private ProjectADomainEntity createEntityWithGeneralTestDocValue(String generalTestDocValue) {
    ProjectADomainEntity entity = new ProjectADomainEntity();
    entity.generalTestDocValue = generalTestDocValue;
    return entity;
  }

  /**************************************************************************************
   * SystemEntity 
   **************************************************************************************/

  @Test
  public void addSystemEntityShouldAddTheEntityToTheDatabase() throws StorageException {
    String id = addSystemEntityWithNameToTheDatabase(GENERAL_STRING_VALUE);

    // verify
    TestSystemEntity foundEntity = instance.getItem(SYSTEM_TYPE, id);
    assertThat(foundEntity, isNotNullSystemType());
    assertThat(foundEntity.getName(), is(equalTo(GENERAL_STRING_VALUE)));
  }

  @Test
  public void updateSystemEntityShouldChangeTheEntity() throws StorageException {
    // setup
    String id = addSystemEntityWithNameToTheDatabase(GENERAL_STRING_VALUE);

    TestSystemEntity entityToUpdate = instance.getItem(SYSTEM_TYPE, id);
    String newName = "newName";
    entityToUpdate.setName(newName);

    // action
    instance.updateSystemEntity(SYSTEM_TYPE, entityToUpdate);

    // verify
    TestSystemEntity updatedEntity = instance.getItem(SYSTEM_TYPE, id);

    assertThat(updatedEntity, isNotNullSystemType());
    assertThat(updatedEntity.getName(), is(equalTo(newName)));
  }

  @Test
  public void deleteRemovesTheEntityFromTheDatabase() throws StorageException {
    // setup
    String id = addSystemEntityWithNameToTheDatabase(GENERAL_STRING_VALUE);

    // check if the entity is saved
    assertThat(instance.getItem(SYSTEM_TYPE, id), isNotNullSystemType());

    // action
    instance.deleteSystemEntity(SYSTEM_TYPE, id);

    // verify
    assertThat(instance.getItem(SYSTEM_TYPE, id), is(nullValue(SYSTEM_TYPE)));
  }

  @Test
  public void getSystemEntitiesReturnsAStorageIteratorWithAllTheEntities() throws StorageException {
    // setup
    int numberOfEntities = 3;
    for (int i = 0; i < numberOfEntities; i++) {
      addSystemEntityWithNameToTheDatabase(GENERAL_STRING_VALUE);
    }

    // action
    StorageIterator<TestSystemEntity> iterator = instance.getSystemEntities(SYSTEM_TYPE);

    // verify
    assertThat(iterator.size(), is(equalTo(numberOfEntities)));

  }

  private String addSystemEntityWithNameToTheDatabase(String name) throws StorageException {
    TestSystemEntity entity = new TestSystemEntity();
    entity.setName(name);

    return instance.addSystemEntity(SYSTEM_TYPE, entity);
  }

  private Matcher<TestSystemEntity> isNotNullSystemType() {
    return is(notNullValue(SYSTEM_TYPE));
  }
}
