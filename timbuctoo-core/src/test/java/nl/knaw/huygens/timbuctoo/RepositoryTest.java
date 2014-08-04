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

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.RelationTypes;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectADomainEntity;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class RepositoryTest {

  private TypeRegistry registryMock;
  private Storage storageMock;
  private Repository repository;
  private Change change;
  private RelationTypes relationTypesMock;

  @Before
  public void setup() throws Exception {
    registryMock = mock(TypeRegistry.class);
    storageMock = mock(Storage.class);
    relationTypesMock = mock(RelationTypes.class);

    repository = new Repository(registryMock, storageMock, relationTypesMock);
    change = new Change("userId", "vreId");
  }

  @Test
  public void testEntityExists() throws Exception {
    repository.entityExists(BaseDomainEntity.class, "id");
    verify(storageMock).entityExists(BaseDomainEntity.class, "id");
  }

  @Test
  public void testGetEntity() throws Exception {
    repository.getEntity(BaseDomainEntity.class, "id");
    verify(storageMock).getItem(BaseDomainEntity.class, "id");
  }

  @Test
  public void testFindEntityByProperty() throws Exception {
    repository.findEntity(TestSystemEntity.class, "field", "value");
    verify(storageMock).findItemByProperty(TestSystemEntity.class, "field", "value");
  }

  @Test
  public void testFindEntity() throws Exception {
    TestSystemEntity entity = new TestSystemEntity();
    repository.findEntity(TestSystemEntity.class, entity);
    verify(storageMock).findItem(TestSystemEntity.class, entity);
  }

  @Test
  public void testGetAllVariations() throws Exception {
    repository.getAllVariations(BaseDomainEntity.class, "id");
    verify(storageMock).getAllVariations(BaseDomainEntity.class, "id");
  }

  @Test
  public void testGetSystemEntities() throws Exception {
    repository.getSystemEntities(TestSystemEntity.class);
    verify(storageMock).getSystemEntities(TestSystemEntity.class);
  }

  @Test
  public void testGetPrimitiveDomainEntities() throws Exception {
    repository.getDomainEntities(BaseDomainEntity.class);
    verify(storageMock).getDomainEntities(BaseDomainEntity.class);
  }

  @Test
  public void testGetProjectDomainEntities() throws Exception {
    repository.getDomainEntities(ProjectADomainEntity.class);
    verify(storageMock).getDomainEntities(ProjectADomainEntity.class);
  }

  @Test
  public void testGetVersions() throws Exception {
    repository.getVersions(BaseDomainEntity.class, "id");
    verify(storageMock).getAllRevisions(BaseDomainEntity.class, "id");
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
    BaseDomainEntity entity = new BaseDomainEntity();
    repository.addDomainEntity(BaseDomainEntity.class, entity, change);
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
    BaseDomainEntity entity = new BaseDomainEntity("id");
    repository.updateDomainEntity(BaseDomainEntity.class, entity, change);
    verify(storageMock).updateDomainEntity(BaseDomainEntity.class, entity, change);
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
    BaseDomainEntity entity = new BaseDomainEntity("id");
    entity.setModified(change);
    repository.deleteDomainEntity(entity);
    verify(storageMock).deleteDomainEntity(BaseDomainEntity.class, "id", change);
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
    verify(storageMock).deleteByDate(SearchResult.class, "date", date);
  }

  @Test
  public void testSetPID() throws Exception {
    repository.setPID(BaseDomainEntity.class, "id", "pid");
    verify(storageMock).setPID(BaseDomainEntity.class, "id", "pid");
  }

  @Test
  public void testDeleteNonPersistent() throws Exception {
    ArrayList<String> ids = Lists.newArrayList("id1", "id2", "id3");
    repository.deleteNonPersistent(BaseDomainEntity.class, ids);
    verify(storageMock).deleteNonPersistent(BaseDomainEntity.class, ids);
  }

  @Test
  public void testGetAllIdsWithoutPID() throws Exception {
    repository.getAllIdsWithoutPID(BaseDomainEntity.class);
    verify(storageMock).getAllIdsWithoutPIDOfType(BaseDomainEntity.class);
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
    when(relationTypesMock.getById(id)).thenReturn(null);
    assertNull(repository.getRelationTypeById(id));
  }

  @Test
  public void testGetRelationTypeWhenItemIsNotInCache() throws Exception {
    String id = "id";
    RelationType type = new RelationType();
    when(relationTypesMock.getById(id)).thenReturn(type);
    assertEquals(type, repository.getRelationTypeById(id));
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

    Repository repository = new Repository(registryMock, storageMock, relationTypesMock);

    when(relationTypesMock.getRelationTypeIdsByName(relationTypeNames)).thenReturn(relationTypeIds);

    // action
    List<String> actualRelationTypeIds = repository.getRelationTypeIdsByName(relationTypeNames);

    // verify
    verify(relationTypesMock).getRelationTypeIdsByName(relationTypeIds);
    assertThat(actualRelationTypeIds, equalTo(relationTypeIds));
  }
}
