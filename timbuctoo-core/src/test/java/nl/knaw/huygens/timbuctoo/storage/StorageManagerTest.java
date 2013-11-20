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
import nl.knaw.huygens.timbuctoo.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.variation.model.GeneralTestDoc;

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
    manager.getEntity(GeneralTestDoc.class, "id");
    verify(storage).getItem(GeneralTestDoc.class, "id");
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
    manager.getVariation(GeneralTestDoc.class, "id", "variation");
    verify(storage).getVariation(GeneralTestDoc.class, "id", "variation");
  }

  @Test
  public void testGetAllVariations() throws IOException {
    manager.getAllVariations(GeneralTestDoc.class, "id");
    verify(storage).getAllVariations(GeneralTestDoc.class, "id");
  }

  @Test
  public void testGetAll() {
    manager.getAll(GeneralTestDoc.class);
    verify(storage).getAllByType(GeneralTestDoc.class);
  }

  @Test
  public void testGetVersions() throws IOException {
    manager.getVersions(GeneralTestDoc.class, "id");
    verify(storage).getAllRevisions(GeneralTestDoc.class, "id");
  }

  @Test
  public void testAddEntity() throws IOException {
    GeneralTestDoc entity = new GeneralTestDoc();
    manager.addEntity(GeneralTestDoc.class, entity);
    verify(storage).addItem(GeneralTestDoc.class, entity);
  }

  @Test
  public void testModifyEntity() throws IOException {
    GeneralTestDoc entity = new GeneralTestDoc("id");
    manager.modifyEntity(GeneralTestDoc.class, entity);
    verify(storage).updateItem(GeneralTestDoc.class, "id", entity);
  }

  @Test
  public void testRemoveSystemEntity() throws IOException {
    TestSystemEntity entity = new TestSystemEntity("id");
    manager.removeEntity(entity);
    verify(storage).removeItem(TestSystemEntity.class, "id");
  }

  @Test
  public void testRemoveDomainEntity() throws IOException {
    GeneralTestDoc entity = new GeneralTestDoc("id");
    Change change = new Change();
    entity.setLastChange(change);
    manager.removeEntity(entity);
    verify(storage).deleteItem(GeneralTestDoc.class, "id", change);
  }

  @Test
  public void testRemoveAllSearchResults() {
    manager.removeAllSearchResults();
    verify(storage).removeAll(SearchResult.class);
  }

  @Test
  public void testRemoveSearchResultsBefore() {
    Date date = new Date();
    manager.removeSearchResultsBefore(date);
    verify(storage).removeByDate(SearchResult.class, "date", date);
  }

  @Test
  public void testSetPID() {
    manager.setPID(GeneralTestDoc.class, "id", "pid");
    verify(storage).setPID(GeneralTestDoc.class, "id", "pid");
  }

  @Test
  public void testRemoveNonPersistent() throws IOException {
    ArrayList<String> ids = Lists.newArrayList("id1", "id2", "id3");
    manager.removeNonPersistent(GeneralTestDoc.class, ids);
    verify(storage).removeNonPersistent(GeneralTestDoc.class, ids);
  }

  @Test
  public void testGetAllIdsWithoutPIDOfType() throws IOException {
    manager.getAllIdsWithoutPIDOfType(GeneralTestDoc.class);
    verify(storage).getAllIdsWithoutPIDOfType(GeneralTestDoc.class);
  }

  @Test
  public void testGetRelationIds() throws IOException {
    ArrayList<String> ids = Lists.newArrayList("id1", "id2", "id3");
    storage.getRelationIds(ids);
    verify(storage).getRelationIds(ids);
  }

  @Test
  public void testGetAllLimited() {
    List<GeneralTestDoc> limitedList = Lists.newArrayList(mock(GeneralTestDoc.class), mock(GeneralTestDoc.class), mock(GeneralTestDoc.class));

    @SuppressWarnings("unchecked")
    StorageIterator<GeneralTestDoc> iterator = mock(StorageIterator.class);
    when(iterator.getSome(anyInt())).thenReturn(limitedList);

    when(storage.getAllByType(GeneralTestDoc.class)).thenReturn(iterator);

    List<GeneralTestDoc> actualList = manager.getAllLimited(GeneralTestDoc.class, 0, 3);
    assertEquals(3, actualList.size());
  }

  @Test
  public void testGetAllLimitedLimitIsZero() {
    List<GeneralTestDoc> list = manager.getAllLimited(GeneralTestDoc.class, 3, 0);
    assertTrue(list.isEmpty());
  }

}
