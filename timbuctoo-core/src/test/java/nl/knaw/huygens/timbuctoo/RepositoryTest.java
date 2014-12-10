package nl.knaw.huygens.timbuctoo;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.EntityMappers;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.RelationTypes;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.util.RelationRefCreatorFactory;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;

import org.junit.Before;
import org.junit.Test;

import test.variation.model.BaseVariationDomainEntity;
import test.variation.model.TestSystemEntity;
import test.variation.model.projecta.ProjectADomainEntity;

import com.google.common.collect.Lists;

public class RepositoryTest {

  private static final String DEFAULT_ID = "TEST000000000001";
  private static final int DEFAULT_REV = 42;

  private TypeRegistry registryMock;
  private Storage storageMock;
  private VRECollection vreCollectionMock;
  private Repository repository;
  private Change change;
  private RelationTypes relationTypesMock;
  private EntityMappers entityMappersMock;
  private RelationRefCreatorFactory relationRefCreatorFactoryMock;

  @Before
  public void setup() throws Exception {
    relationTypesMock = mock(RelationTypes.class);
    registryMock = mock(TypeRegistry.class);
    vreCollectionMock = mock(VRECollection.class);
    storageMock = mock(Storage.class);
    entityMappersMock = mock(EntityMappers.class);
    relationRefCreatorFactoryMock = mock(RelationRefCreatorFactory.class);

    repository = new Repository(registryMock, storageMock, vreCollectionMock, relationTypesMock, entityMappersMock, relationRefCreatorFactoryMock);
    change = new Change("userId", "vreId");
  }

  @Test
  public void testEntityExists() throws Exception {
    repository.entityExists(BaseVariationDomainEntity.class, "id");
    verify(storageMock).entityExists(BaseVariationDomainEntity.class, "id");
  }

  @Test
  public void testGetEntity() throws Exception {
    repository.getEntity(BaseVariationDomainEntity.class, "id");
    verify(storageMock).getItem(BaseVariationDomainEntity.class, "id");
  }

  @Test
  public void testFindEntityByProperty() throws Exception {
    repository.findEntity(TestSystemEntity.class, "field", "value");
    verify(storageMock).findItemByProperty(TestSystemEntity.class, "field", "value");
  }

  @Test
  public void testGetAllVariations() throws Exception {
    List<BaseVariationDomainEntity> entities = Lists.newArrayList();
    Class<BaseVariationDomainEntity> type = BaseVariationDomainEntity.class;
    when(storageMock.getAllVariations(type, DEFAULT_ID)).thenReturn(entities);

    repository.getAllVariations(type, DEFAULT_ID);
    verify(storageMock).getAllVariations(type, DEFAULT_ID);
  }

  @Test
  public void testGetEntityWithRelations() throws Exception {
    repository.getEntityWithRelations(BaseVariationDomainEntity.class, DEFAULT_ID);
    verify(storageMock).getItem(BaseVariationDomainEntity.class, DEFAULT_ID);
  }

  @Test
  public void testGetRevisionWithRelations() throws Exception {
    repository.getRevisionWithRelations(BaseVariationDomainEntity.class, DEFAULT_ID, DEFAULT_REV);
    verify(storageMock).getRevision(BaseVariationDomainEntity.class, DEFAULT_ID, DEFAULT_REV);
  }

  @Test
  public void testGetSystemEntities() throws Exception {
    repository.getSystemEntities(TestSystemEntity.class);
    verify(storageMock).getSystemEntities(TestSystemEntity.class);
  }

  @Test
  public void testGetPrimitiveDomainEntities() throws Exception {
    repository.getDomainEntities(BaseVariationDomainEntity.class);
    verify(storageMock).getDomainEntities(BaseVariationDomainEntity.class);
  }

  @Test
  public void testGetProjectDomainEntities() throws Exception {
    repository.getDomainEntities(ProjectADomainEntity.class);
    verify(storageMock).getDomainEntities(ProjectADomainEntity.class);
  }

  @Test
  public void testGetVersions() throws Exception {
    repository.getVersions(BaseVariationDomainEntity.class, "id");
    verify(storageMock).getAllRevisions(BaseVariationDomainEntity.class, "id");
  }

  @Test
  public void testAddSystemEntity() throws Exception {
    TestSystemEntity entity = mock(TestSystemEntity.class);
    repository.addSystemEntity(TestSystemEntity.class, entity);
    verify(entity).validateForAdd(repository);
    verify(storageMock).addSystemEntity(TestSystemEntity.class, entity);
  }

  @Test(expected = ValidationException.class)
  public void testAddPrimitiveDomainEntity() throws Exception {
    BaseVariationDomainEntity entity = new BaseVariationDomainEntity();
    repository.addDomainEntity(BaseVariationDomainEntity.class, entity, change);
  }

  @Test
  public void testAddDerivedDomainEntity() throws Exception {
    ProjectADomainEntity entity = mock(ProjectADomainEntity.class);
    repository.addDomainEntity(ProjectADomainEntity.class, entity, change);
    verify(entity).validateForAdd(repository);
    verify(storageMock).addDomainEntity(ProjectADomainEntity.class, entity, change);
  }

  @Test(expected = ValidationException.class)
  public void testAddInvalidDerivedDomainEntity() throws Exception {
    ProjectADomainEntity entity = mock(ProjectADomainEntity.class);
    doThrow(ValidationException.class).when(entity).validateForAdd(repository);
    repository.addDomainEntity(ProjectADomainEntity.class, entity, change);
  }

  @Test
  public void testUpdatePrimitiveDomainEntity() throws Exception {
    BaseVariationDomainEntity entity = new BaseVariationDomainEntity("id");
    repository.updateDomainEntity(BaseVariationDomainEntity.class, entity, change);
    verify(storageMock).updateDomainEntity(BaseVariationDomainEntity.class, entity, change);
  }

  @Test
  public void testUpdateProjectDomainEntity() throws Exception {
    ProjectADomainEntity entity = new ProjectADomainEntity("id");
    repository.updateDomainEntity(ProjectADomainEntity.class, entity, change);
    verify(storageMock).updateDomainEntity(ProjectADomainEntity.class, entity, change);
  }

  @Test
  public void testDeleteSystemEntity() throws Exception {
    TestSystemEntity entity = new TestSystemEntity("id");
    repository.deleteSystemEntity(entity);
    verify(storageMock).deleteSystemEntity(TestSystemEntity.class, "id");
  }

  @Test
  public void testDeleteDomainEntity() throws Exception {
    BaseVariationDomainEntity entity = new BaseVariationDomainEntity("id");
    entity.setModified(change);
    repository.deleteDomainEntity(entity);
    verify(storageMock).deleteDomainEntity(BaseVariationDomainEntity.class, "id", change);
  }

  @Test
  public void testDeleteAllSearchResults() throws Exception {
    repository.deleteAllSearchResults();
    verify(storageMock).deleteSystemEntities(SearchResult.class);
  }

  @Test
  public void testDeleteSearchResultsBefore() throws Exception {
    Date date = new Date();
    repository.deleteSearchResultsBefore(date);
    verify(storageMock).deleteByModifiedDate(SearchResult.class, date);
  }

  @Test
  public void testSetPID() throws Exception {
    repository.setPID(BaseVariationDomainEntity.class, "id", "pid");
    verify(storageMock).setPID(BaseVariationDomainEntity.class, "id", "pid");
  }

  @Test
  public void testDeleteNonPersistent() throws Exception {
    ArrayList<String> ids = Lists.newArrayList("id1", "id2", "id3");
    repository.deleteNonPersistent(BaseVariationDomainEntity.class, ids);
    verify(storageMock).deleteNonPersistent(BaseVariationDomainEntity.class, ids);
  }

  @Test
  public void testGetAllIdsWithoutPID() throws Exception {
    repository.getAllIdsWithoutPID(BaseVariationDomainEntity.class);
    verify(storageMock).getAllIdsWithoutPIDOfType(BaseVariationDomainEntity.class);
  }

  @Test
  public void testGetRelationIds() throws Exception {
    ArrayList<String> ids = Lists.newArrayList("id1", "id2", "id3");
    storageMock.getRelationIds(ids);
    verify(storageMock).getRelationIds(ids);
  }

  @Test
  public void testGetRelationTypeWhenItemIsUnknown() throws Exception {
    String id = "id";
    when(relationTypesMock.getById(id, false)).thenReturn(null);
    assertNull(repository.getRelationTypeById(id, false));
  }

  @Test
  public void testGetRelationTypeWhenItemIsNotInCache() throws Exception {
    String id = "id";
    RelationType type = new RelationType();
    when(relationTypesMock.getById(id, false)).thenReturn(type);
    assertEquals(type, repository.getRelationTypeById(id, false));
  }

  @Test
  public void testGetRelationsByType() throws Exception {
    // setup
    List<String> relationTypeIds = Lists.newArrayList();
    List<Relation> relations = Lists.newArrayList();
    final Class<Relation> type = Relation.class;
    when(storageMock.getRelationsByType(type, relationTypeIds)).thenReturn(relations);

    // action
    List<Relation> actualRelations = repository.getRelationsByType(type, relationTypeIds);

    // verify
    verify(storageMock).getRelationsByType(type, relationTypeIds);
    assertThat(actualRelations, equalTo(relations));
  }

  @Test
  public void testGetRelationTypeIdsByName() throws Exception {
    // setup
    List<String> relationTypeNames = Lists.newArrayList();
    List<String> relationTypeIds = Lists.newArrayList();

    when(relationTypesMock.getRelationTypeIdsByName(relationTypeNames)).thenReturn(relationTypeIds);

    // action
    List<String> actualRelationTypeIds = repository.getRelationTypeIdsByName(relationTypeNames);

    // verify
    verify(relationTypesMock).getRelationTypeIdsByName(relationTypeIds);
    assertThat(actualRelationTypeIds, equalTo(relationTypeIds));
  }
}
