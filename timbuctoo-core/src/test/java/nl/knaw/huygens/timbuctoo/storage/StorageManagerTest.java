package nl.knaw.huygens.timbuctoo.storage;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectADomainEntity;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class StorageManagerTest {

  private TypeRegistry registry;
  private Storage storage;
  private StorageManager manager;
  private Change change;

  @Before
  public void setup() {
    registry = mock(TypeRegistry.class);
    storage = mock(Storage.class);
    manager = new StorageManager(registry, storage);
    change = new Change("userId", "vreId");
  }

  @Test
  public void testGetEntity() throws IOException {
    manager.getEntity(BaseDomainEntity.class, "id");
    verify(storage).getItem(BaseDomainEntity.class, "id");
  }

  @Test
  public void testFindEntityByProperty() throws IOException {
    manager.findEntity(TestSystemEntity.class, "field", "value");
    verify(storage).findItemByProperty(TestSystemEntity.class, "field", "value");
  }

  @Test
  public void testFindEntity() throws IOException {
    TestSystemEntity entity = new TestSystemEntity();
    manager.findEntity(TestSystemEntity.class, entity);
    verify(storage).findItem(TestSystemEntity.class, entity);
  }

  @Test
  public void testGetAllVariations() throws IOException {
    manager.getAllVariations(BaseDomainEntity.class, "id");
    verify(storage).getAllVariations(BaseDomainEntity.class, "id");
  }

  @Test
  public void testGetAll() {
    manager.getAll(BaseDomainEntity.class);
    verify(storage).getAllByType(BaseDomainEntity.class);
  }

  @Test
  public void testGetVersions() throws IOException {
    manager.getVersions(BaseDomainEntity.class, "id");
    verify(storage).getAllRevisions(BaseDomainEntity.class, "id");
  }

  @Test
  public void testAddSystemEntity() throws Exception {
    TestSystemEntity entity = mock(TestSystemEntity.class);
    manager.addSystemEntity(TestSystemEntity.class, entity);
    verify(entity).validateForAdd(registry, manager);
    verify(storage).addSystemEntity(TestSystemEntity.class, entity);
  }

  @Test(expected = ValidationException.class)
  public void testAddPrimitiveDomainEntity() throws Exception {
    BaseDomainEntity entity = new BaseDomainEntity();
    manager.addDomainEntity(BaseDomainEntity.class, entity, change);
  }

  @Test
  public void testAddDerivedDomainEntity() throws Exception {
    ProjectADomainEntity entity = mock(ProjectADomainEntity.class);
    manager.addDomainEntity(ProjectADomainEntity.class, entity, change);
    verify(entity).validateForAdd(registry, manager);
    verify(storage).addDomainEntity(ProjectADomainEntity.class, entity, change);
  }

  @Test(expected = ValidationException.class)
  public void testAddInvalidDerivedDomainEntity() throws IOException, ValidationException {
    ProjectADomainEntity entity = mock(ProjectADomainEntity.class);
    doThrow(ValidationException.class).when(entity).validateForAdd(registry, manager);
    manager.addDomainEntity(ProjectADomainEntity.class, entity, change);
  }

  @Test
  public void testUpdatePrimitiveDomainEntity() throws IOException {
    BaseDomainEntity entity = new BaseDomainEntity("id");
    manager.updatePrimitiveDomainEntity(BaseDomainEntity.class, entity, change);
    verify(storage).updateDomainEntity(BaseDomainEntity.class, entity, change);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdatePrimitiveDomainEntityWithWrongType() throws IOException {
    ProjectADomainEntity entity = new ProjectADomainEntity("id");
    manager.updatePrimitiveDomainEntity(ProjectADomainEntity.class, entity, change);
  }

  @Test
  public void testUpdateProjectDomainEntity() throws IOException {
    ProjectADomainEntity entity = new ProjectADomainEntity("id");
    manager.updateProjectDomainEntity(ProjectADomainEntity.class, entity, change);
    verify(storage).updateDomainEntity(ProjectADomainEntity.class, entity, change);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateProjectDomainEntityWithWrongType() throws IOException {
    BaseDomainEntity entity = new BaseDomainEntity("id");
    manager.updateProjectDomainEntity(BaseDomainEntity.class, entity, change);
  }

  @Test
  public void testDeleteSystemEntity() throws IOException {
    TestSystemEntity entity = new TestSystemEntity("id");
    manager.deleteSystemEntity(entity);
    verify(storage).deleteSystemEntity(TestSystemEntity.class, "id");
  }

  @Test
  public void testDeleteDomainEntity() throws IOException {
    BaseDomainEntity entity = new BaseDomainEntity("id");
    entity.setModified(change);
    manager.deleteDomainEntity(entity);
    verify(storage).deleteDomainEntity(BaseDomainEntity.class, "id", change);
  }

  @Test
  public void testDeleteAllSearchResults() throws IOException {
    manager.deleteAllSearchResults();
    verify(storage).deleteAll(SearchResult.class);
  }

  @Test
  public void testDeleteSearchResultsBefore() throws IOException {
    Date date = new Date();
    manager.deleteSearchResultsBefore(date);
    verify(storage).deleteByDate(SearchResult.class, "date", date);
  }

  @Test
  public void testSetPID() throws IOException {
    manager.setPID(BaseDomainEntity.class, "id", "pid");
    verify(storage).setPID(BaseDomainEntity.class, "id", "pid");
  }

  @Test
  public void testDeleteNonPersistent() throws IOException {
    ArrayList<String> ids = Lists.newArrayList("id1", "id2", "id3");
    manager.deleteNonPersistent(BaseDomainEntity.class, ids);
    verify(storage).deleteNonPersistent(BaseDomainEntity.class, ids);
  }

  @Test
  public void testGetAllIdsWithoutPIDOfType() throws IOException {
    manager.getAllIdsWithoutPIDOfType(BaseDomainEntity.class);
    verify(storage).getAllIdsWithoutPIDOfType(BaseDomainEntity.class);
  }

  @Test
  public void testGetRelationIds() throws IOException {
    ArrayList<String> ids = Lists.newArrayList("id1", "id2", "id3");
    storage.getRelationIds(ids);
    verify(storage).getRelationIds(ids);
  }

  @Test
  public void testGetAllLimited() {
    List<BaseDomainEntity> limitedList = Lists.newArrayList(mock(BaseDomainEntity.class), mock(BaseDomainEntity.class), mock(BaseDomainEntity.class));

    @SuppressWarnings("unchecked")
    StorageIterator<BaseDomainEntity> iterator = mock(StorageIterator.class);
    when(iterator.skip(anyInt())).thenReturn(iterator);
    when(iterator.getSome(anyInt())).thenReturn(limitedList);

    when(storage.getAllByType(BaseDomainEntity.class)).thenReturn(iterator);

    List<BaseDomainEntity> actualList = manager.getAllLimited(BaseDomainEntity.class, 0, 3);
    assertEquals(3, actualList.size());
  }

  @Test
  public void testGetAllLimitedLimitIsZero() {
    List<BaseDomainEntity> list = manager.getAllLimited(BaseDomainEntity.class, 3, 0);
    assertTrue(list.isEmpty());
  }

  @Test
  public void getAllByIds() {
    List<BaseDomainEntity> limitedList = Lists.newArrayList(mock(BaseDomainEntity.class), mock(BaseDomainEntity.class), mock(BaseDomainEntity.class));

    @SuppressWarnings("unchecked")
    StorageIterator<BaseDomainEntity> iterator = mock(StorageIterator.class);
    when(iterator.getSome(anyInt())).thenReturn(limitedList);

    ArrayList<String> ids = Lists.newArrayList("id1", "id2", "id3");
    when(storage.getEntitiesByIds(BaseDomainEntity.class, ids)).thenReturn(iterator);

    List<BaseDomainEntity> actualList = manager.getAllByIds(BaseDomainEntity.class, ids);
    verify(iterator).getSome(3);
    assertEquals(3, actualList.size());
  }

  @Test
  public void testGetRelationTypeWhenExceptionOccurs() throws Exception {
    String id = "id";
    when(storage.getItem(RelationType.class, id)).thenThrow(new IOException());
    assertNull(manager.getRelationType(id));
    verify(storage, times(1)).getItem(RelationType.class, id);
  }

  @Test
  public void testGetRelationTypeWhenItemIsUnknown() throws Exception {
    String id = "id";
    when(storage.getItem(RelationType.class, id)).thenReturn(null);
    assertNull(manager.getRelationType(id));
    verify(storage, times(1)).getItem(RelationType.class, id);
  }

  @Test
  public void testGetRelationTypeWhenItemIsNotInCache() throws Exception {
    String id = "id";
    RelationType type = new RelationType();
    when(storage.getItem(RelationType.class, id)).thenReturn(type);
    assertEquals(type, manager.getRelationType(id));
    verify(storage, times(1)).getItem(RelationType.class, id);
  }

  @Test
  public void testGetRelationTypeWhenItemIsInCache() throws Exception {
    String id = "id";
    RelationType type = new RelationType();
    when(storage.getItem(RelationType.class, id)).thenReturn(type);
    manager.getRelationType(id);
    assertEquals(type, manager.getRelationType(id));
    verify(storage, times(1)).getItem(RelationType.class, id);
  }

}
