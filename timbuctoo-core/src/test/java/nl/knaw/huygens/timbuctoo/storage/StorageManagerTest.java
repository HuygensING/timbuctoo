package nl.knaw.huygens.timbuctoo.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;
import nl.knaw.huygens.timbuctoo.variation.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.projecta.ProjectADomainEntity;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class StorageManagerTest {

  private Configuration config;
  private Storage storage;
  private StorageManager manager;

  @Before
  public void setup() {
    config = mock(Configuration.class);
    storage = mock(Storage.class);
    manager = new StorageManager(config, storage);
  }

  @Test
  public void testGetEntity() throws IOException {
    manager.getEntity(BaseDomainEntity.class, "id");
    verify(storage).getItem(BaseDomainEntity.class, "id");
  }

  @Test
  public void testFindEntityByKey() throws IOException {
    manager.findEntity(TestSystemEntity.class, "key", "value");
    verify(storage).findItemByKey(TestSystemEntity.class, "key", "value");
  }

  @Test
  public void testFindEntity() throws IOException {
    TestSystemEntity entity = new TestSystemEntity();
    manager.findEntity(TestSystemEntity.class, entity);
    verify(storage).findItem(TestSystemEntity.class, entity);
  }

  @Test
  public void testGetVariation() throws IOException {
    manager.getVariation(BaseDomainEntity.class, "id", "variation");
    verify(storage).getVariation(BaseDomainEntity.class, "id", "variation");
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

  @Test(expected = IllegalArgumentException.class)
  public void testAddPrimitiveDomainEntity() throws IOException {
    BaseDomainEntity entity = new BaseDomainEntity();
    Change change = Change.newInstance();
    manager.addDomainEntity(BaseDomainEntity.class, entity, change);
  }

  @Test
  public void testAddDerivedDomainEntity() throws IOException {
    ProjectADomainEntity entity = new ProjectADomainEntity();
    Change change = Change.newInstance();
    manager.addDomainEntity(ProjectADomainEntity.class, entity, change);
    verify(storage).addDomainEntity(ProjectADomainEntity.class, entity, change);
  }

  @Test
  public void testModifyEntity() throws IOException {
    BaseDomainEntity entity = new BaseDomainEntity("id");
    Change change = Change.newInstance();
    manager.updateDomainEntity(BaseDomainEntity.class, entity, change);
    verify(storage).updateDomainEntity(BaseDomainEntity.class, "id", entity, change);
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
    Change change = Change.newInstance();
    entity.setModified(change);
    manager.deleteDomainEntity(entity);
    verify(storage).deleteDomainEntity(BaseDomainEntity.class, "id", change);
  }

  @Test
  public void testDeleteAllSearchResults() {
    manager.deleteAllSearchResults();
    verify(storage).deleteAll(SearchResult.class);
  }

  @Test
  public void testDeleteSearchResultsBefore() {
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

}
