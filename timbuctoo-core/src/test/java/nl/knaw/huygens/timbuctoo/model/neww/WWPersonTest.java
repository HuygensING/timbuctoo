package nl.knaw.huygens.timbuctoo.model.neww;

import static nl.knaw.huygens.timbuctoo.model.AddRelationsTestHelper.ENTITY_ID;
import static nl.knaw.huygens.timbuctoo.model.AddRelationsTestHelper.RELATION_LIMIT;
import static nl.knaw.huygens.timbuctoo.model.AddRelationsTestHelper.createRelation;
import static nl.knaw.huygens.timbuctoo.model.AddRelationsTestHelper.createRelationWhereEntityIsSource;
import static nl.knaw.huygens.timbuctoo.model.AddRelationsTestHelper.createRelationWhereEntityIsTarget;
import static nl.knaw.huygens.timbuctoo.model.AddRelationsTestHelper.setupEntityMappers;
import static nl.knaw.huygens.timbuctoo.model.AddRelationsTestHelper.setupRepositoryWithRelationsForEntity;
import static nl.knaw.huygens.timbuctoo.model.AddRelationsTestHelper.verifyRelationRefIsCreatedForRelation;
import static nl.knaw.huygens.timbuctoo.model.neww.WWPerson.HAS_WORK_LANGUAGE_RELATION;
import static nl.knaw.huygens.timbuctoo.model.neww.WWPerson.IS_CREATED_BY_RELATION;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.config.EntityMappers;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.util.RelationRefCreator;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

public class WWPersonTest {
  private static final String LANGUAGE_X_TYPE = "languages";
  private static final String LANGUAGE_TYPE = "language";
  private static final String HAS_WORK_LANGUAGE_ID = "hasWorkLanguageId";
  private static final String CREATED_BY_ID = "createdById";
  private WWPerson instance;
  private EntityMapper entityMapperMock;
  private RelationRefCreator relationRefCreatorMock;
  private EntityMappers entityMappersMock;

  @Before
  public void setUp() {
    instance = new WWPerson();
    instance.setId(ENTITY_ID);

    entityMapperMock = mock(EntityMapper.class);
    entityMappersMock = setupEntityMappers(instance.getClass(), entityMapperMock);
    relationRefCreatorMock = mock(RelationRefCreator.class);
  }

  @Test
  public void addRelationsShouldAddDirectRelationsAndIsCreatedByAndHasWorkLanguageIndirect() throws StorageException {
    // setup
    Relation relation1 = createRelationWhereEntityIsSource(ENTITY_ID, 1);
    Relation relation2 = createRelationWhereEntityIsTarget(ENTITY_ID, 2);
    Repository repositoryMock = setupRepositoryWithRelationsForEntity(ENTITY_ID, relation1, relation2);
    String languageId = "id123";
    String displayName = "lang";

    setupGetRelationTypeByName(repositoryMock, IS_CREATED_BY_RELATION, CREATED_BY_ID);

    String workId1 = "workdId";
    setupCreatedBy(repositoryMock, ENTITY_ID, workId1);

    setUpHasWorkLanguage(repositoryMock, workId1, languageId);

    setupGetRelationTypeByName(repositoryMock, HAS_WORK_LANGUAGE_RELATION, HAS_WORK_LANGUAGE_ID);

    Language language = new Language();
    language.setId(languageId);
    language.setName(displayName);

    when(repositoryMock.getEntity(Language.class, languageId)).thenReturn(language);

    // action
    instance.addRelations(repositoryMock, RELATION_LIMIT, entityMappersMock, relationRefCreatorMock);

    // verify
    verifyRelationRefIsCreatedForRelation(relation1, true, relationRefCreatorMock, entityMapperMock);
    verifyRelationRefIsCreatedForRelation(relation2, false, relationRefCreatorMock, entityMapperMock);

    verify(relationRefCreatorMock).newRelationRef(LANGUAGE_TYPE, LANGUAGE_X_TYPE, languageId, displayName, null, true, 0);
  }

  private void setupCreatedBy(Repository repositoryMock, String entityId, String... workIds) throws StorageException {
    final List<Relation> relations = Lists.newArrayList();

    for (String workId : workIds) {
      relations.add(createRelation(workId, entityId, null, CREATED_BY_ID));
    }

    when(repositoryMock.findRelations(null, entityId, CREATED_BY_ID)).thenReturn(createStorageIterator(relations));
  }

  private void setUpHasWorkLanguage(Repository repositoryMock, String workId, String... languageIds) throws StorageException {
    final List<Relation> relations = Lists.newArrayList();

    for (String languageId : languageIds) {
      relations.add(createRelation(workId, languageId, null, HAS_WORK_LANGUAGE_ID));
    }

    when(repositoryMock.findRelations(workId, null, HAS_WORK_LANGUAGE_ID)).thenReturn(createStorageIterator(relations));
  }

  protected StorageIterator<Relation> createStorageIterator(final List<Relation> relations) {
    return new StorageRelationListItertator(relations.iterator());
  }

  private void setupGetRelationTypeByName(Repository repositoryMock, String relationTypeName, String relationTypeId) throws StorageException {
    RelationType relationType = new RelationType();
    relationType.setId(relationTypeId);

    when(repositoryMock.getRelationTypeByName(relationTypeName)).thenReturn(relationType);
  }

  @Ignore
  @Test
  public void addRelationsThrowsAnExceptionIfRepositoryThrowsAnExceptionWhileRetrievingTheRelations() {
    fail("Yet to be implemented");
  }

  @Ignore
  @Test
  public void addRelationsThrowsAnExceptionIfRepositoryThrowsAnExceptionWhileRetrievingRelationTypes() {
    fail("Yet to be implemented");
  }

  @Ignore
  @Test(expected = StorageException.class)
  public void addRelationsThrowsAnExceptionIfRelationRefCreatorThrowsAnException() throws StorageException {
    fail("Yet to be implemented");
  }

  @Ignore
  @Test(expected = StorageException.class)
  public void addRelationsDoesNotAddDerivedEntitiesWhenLimitIsZero() throws StorageException {
    fail("Yet to be implemented");
  }

  private static class StorageRelationListItertator implements StorageIterator<Relation> {
    private Iterator<Relation> relationIterator;

    public StorageRelationListItertator(Iterator<Relation> relationIterator) {
      this.relationIterator = relationIterator;
    }

    @Override
    public boolean hasNext() {
      // TODO Auto-generated method stub
      return relationIterator.hasNext();
    }

    @Override
    public Relation next() {
      // TODO Auto-generated method stub
      return relationIterator.next();
    }

    @Override
    public void remove() {
      // TODO Auto-generated method stub

    }

    @Override
    public int size() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public StorageIterator<Relation> skip(int count) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<Relation> getSome(int limit) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<Relation> getAll() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void close() {
      // TODO Auto-generated method stub

    }

  }
}
