package nl.knaw.huygens.timbuctoo.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.TestSystemEntity;
import nl.knaw.huygens.timbuctoo.variation.model.GeneralTestDoc;
import nl.knaw.huygens.timbuctoo.variation.model.TestConcreteDoc;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import com.google.common.collect.Lists;

public class StorageManagerTest {

  private Configuration config;
  private StorageManager instance;
  private VariationStorage storage;
  private TypeRegistry typeRegistry;

  @Before
  public void SetUp() {
    config = mock(Configuration.class);
    storage = mock(VariationStorage.class);
    typeRegistry = mock(TypeRegistry.class);
    instance = new StorageManager(config, storage);
  }

  @Test
  public void testAddDocumentDomainDocument() throws IOException {
    String id = "TEST000123000123";
    GeneralTestDoc doc = new GeneralTestDoc(id);
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    when(typeRegistry.getINameForType(type)).thenReturn("generaltestdoc");

    instance.addEntity(type, doc);

    verifyAddDocument(type, doc, times(1), times(1));
  }

  @Test
  public void testAddDocumentSystemDocument() throws IOException, PersistenceException {
    TestSystemEntity doc = new TestSystemEntity();
    doc.setId("TEST000123000123");

    Class<TestSystemEntity> type = TestSystemEntity.class;

    instance.addEntity(type, doc);

    verifyAddDocument(type, doc, times(1), never());
  }

  @Test(expected = IOException.class)
  public void testAddDocumentStorageException() throws IOException, PersistenceException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    doThrow(IOException.class).when(storage).addItem(type, doc);

    instance.addEntity(type, doc);
  }

  protected <T extends Entity> void verifyAddDocument(Class<T> type, T doc, VerificationMode storageVerification, VerificationMode indexingVerification) throws IOException {

    verify(storage, storageVerification).addItem(type, doc);
  }

  @Test
  public void testGetDocumentDocumentFound() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";

    GeneralTestDoc doc = mock(GeneralTestDoc.class);
    when(doc.getId()).thenReturn(id);
    when(doc.getDisplayName()).thenReturn("test");

    when(storage.getItem(type, id)).thenReturn(doc);

    Entity actualDoc = instance.getEntity(type, id);

    assertEquals(id, actualDoc.getId());
    assertEquals("test", actualDoc.getDisplayName());
  }

  @Test
  public void testGetDocumentDocumentNotFound() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";

    when(storage.getItem(type, id)).thenReturn(null);

    assertNull(instance.getEntity(type, id));
  }

  @Test
  public void testGetDocumentIOException() throws IOException {
    when(storage.getItem(GeneralTestDoc.class, "testId")).thenThrow(new IOException("Thrown by unit test"));
    assertNull(instance.getEntity(GeneralTestDoc.class, "testId"));
  }

  @Test
  public void testGetCompleteVariationDocumentFound() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";
    String variation = "projecta";

    GeneralTestDoc doc = mock(GeneralTestDoc.class);
    when(doc.getId()).thenReturn(id);
    when(doc.getDisplayName()).thenReturn("test");

    when(storage.getVariation(type, id, variation)).thenReturn(doc);

    Entity actualDoc = instance.getCompleteVariation(type, id, variation);

    assertEquals(id, actualDoc.getId());
    assertEquals("test", actualDoc.getDisplayName());
  }

  @Test()
  public void testGetCompleteVariationDocumentNotFound() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";
    String variation = "projecta";

    when(storage.getVariation(type, id, variation)).thenReturn(null);

    assertNull(instance.getCompleteVariation(type, id, variation));
  }

  @SuppressWarnings("unchecked")
  @Test()
  public void testGetCompleteVariationIOException() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";
    String variation = "projecta";

    when(storage.getVariation(type, id, variation)).thenThrow(IOException.class);

    assertNull(instance.getCompleteVariation(type, id, variation));
  }

  @Test
  public void testGetAllVariationsSuccess() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";

    GeneralTestDoc doc = mock(GeneralTestDoc.class);
    when(doc.getId()).thenReturn(id);
    when(doc.getDisplayName()).thenReturn("test");

    GeneralTestDoc doc2 = mock(GeneralTestDoc.class);
    when(doc.getId()).thenReturn(id);
    when(doc.getDisplayName()).thenReturn("test2");

    when(storage.getAllVariations(type, id)).thenReturn(Lists.newArrayList(doc, doc2));

    List<GeneralTestDoc> actualDocs = instance.getAllVariations(type, id);

    assertEquals(2, actualDocs.size());
  }

  @Test
  public void testGetAllVariationsDocumentNull() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";

    when(storage.getAllVariations(type, id)).thenReturn(null);

    assertNull(instance.getAllVariations(type, id));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetAllVariationsIOException() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";

    when(storage.getAllVariations(type, id)).thenThrow(IOException.class);

    assertNull(instance.getAllVariations(type, id));
  }

  @Test
  public void testModifyDocumentDomainDocumentModified() throws IOException {
    TestConcreteDoc expectedDoc = new TestConcreteDoc();
    expectedDoc.name = "test";
    String id = "TCD0000000001";
    expectedDoc.setId(id);

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    instance.modifyEntity(type, expectedDoc);
    verifyModifyDocument(type, expectedDoc, times(1), times(1));
  }

  @Test
  public void testModifyDocumentSystemDocumentModified() throws IOException {
    String id = "TSD0000000001";
    TestSystemEntity expectedDoc = new TestSystemEntity();
    expectedDoc.setId(id);

    Class<TestSystemEntity> type = TestSystemEntity.class;

    instance.modifyEntity(type, expectedDoc);
    verifyModifyDocument(type, expectedDoc, times(1), never());
  }

  @Test(expected = IOException.class)
  public void testModifyDocumentStorageException() throws IOException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    doc.setId("TCD0000000001");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    doThrow(IOException.class).when(storage).updateItem(type, doc.getId(), doc);

    instance.modifyEntity(type, doc);
  }

  @Test
  public void testModifyDocumentPersistentException() throws IOException {
    TestConcreteDoc expectedDoc = new TestConcreteDoc();
    expectedDoc.name = "test";
    expectedDoc.setId("TCD0000000001");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    instance.modifyEntity(type, expectedDoc);
    verifyModifyDocument(type, expectedDoc, times(1), times(1));
  }

  protected <T extends Entity> void verifyModifyDocument(Class<T> type, T expectedDoc, VerificationMode storageVerification, VerificationMode indexVerification) throws IOException {

    verify(storage, storageVerification).updateItem(type, expectedDoc.getId(), expectedDoc);
  }

  @Test
  public void testRemoveDocumentDomainDocumentRemoved() throws IOException {
    TestConcreteDoc inputDoc = new TestConcreteDoc();
    inputDoc.name = "test";
    String id = "TCD0000000001";
    inputDoc.setId(id);
    inputDoc.setDeleted(true);

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    String typeString = "testconcretedoc";

    when(typeRegistry.getINameForType(Mockito.<Class<? extends Entity>> any())).thenReturn(typeString);

    instance.removeEntity(type, inputDoc);
    verify(storage).deleteItem(type, inputDoc.getId(), inputDoc.getLastChange());
  }

  @Test
  public void testRemoveDocumentSystemDocumentRemoved() throws IOException {
    TestSystemEntity inputDoc = new TestSystemEntity();
    String id = "TCD0000000001";
    inputDoc.setId(id);
    inputDoc.setDeleted(true);

    Class<TestSystemEntity> type = TestSystemEntity.class;

    instance.removeEntity(type, inputDoc);
    verify(storage, times(1)).deleteItem(type, inputDoc.getId(), inputDoc.getLastChange());
  }

  @Test(expected = IOException.class)
  public void testRemoveDocumentStorageException() throws IOException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    doc.setId("TCD0000000001");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    doThrow(IOException.class).when(storage).deleteItem(type, doc.getId(), doc.getLastChange());

    instance.removeEntity(type, doc);
  }

  @Test
  public void testRemovePermanently() throws IOException {
    String id1 = "PER0000000001";
    String id2 = "PER0000000002";
    String id3 = "PER0000000005";
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String typeString = "generaltestdoc";

    ArrayList<String> ids = Lists.newArrayList(id1, id2, id3);
    when(typeRegistry.getINameForType(type)).thenReturn(typeString);

    instance.removeNonPersistent(type, ids);

    verify(storage, times(1)).removeNonPersistent(type, ids);
  }

  @Test(expected = IOException.class)
  public void testRemovePermanentlyStorageDeleteException() throws IOException {
    String id1 = "PER0000000001";
    String id2 = "PER0000000002";
    String id3 = "PER0000000005";
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String typeString = "generaltestdoc";

    ArrayList<String> ids = Lists.newArrayList(id1, id2, id3);

    doThrow(IOException.class).when(storage).removeNonPersistent(type, ids);
    when(typeRegistry.getINameForType(type)).thenReturn(typeString);

    try {
      instance.removeNonPersistent(type, ids);
    } finally {
      verify(storage, times(1)).removeNonPersistent(type, ids);
    }
  }

  @Test
  public void testGetAllIdsWithoutPIDOfType() throws IOException {
    ArrayList<String> expected = Lists.newArrayList("PER0000000001", "PER0000000002", "PER0000000005");

    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    when(storage.getAllIdsWithoutPIDOfType(type)).thenReturn(expected);

    List<String> actual = instance.getAllIdsWithoutPIDOfType(type);

    assertEquals(expected, actual);
  }

  @Test(expected = IOException.class)
  public void testGetAllIdsWithoutPIDOfTypeStorageException() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    doThrow(IOException.class).when(storage).getAllIdsWithoutPIDOfType(type);

    instance.getAllIdsWithoutPIDOfType(type);
  }

  @Test
  public void testGetRelationIds() throws IOException {
    List<String> ids = Lists.newArrayList("PER0000000001", "PER0000000002", "PER0000000005");

    storage.getRelationIds(ids);

    verify(storage).getRelationIds(ids);
  }

  @Test(expected = IOException.class)
  public void testGetRelationIdsStorageThrowsException() throws IOException {
    List<String> ids = Lists.newArrayList("PER0000000001", "PER0000000002", "PER0000000005");

    doThrow(IOException.class).when(storage).getRelationIds(ids);

    storage.getRelationIds(ids);
  }

  @Test
  public void testGetAllLimited() {
    List<TestConcreteDoc> limitedList = Lists.newArrayList(mock(TestConcreteDoc.class), mock(TestConcreteDoc.class), mock(TestConcreteDoc.class));

    @SuppressWarnings("unchecked")
    StorageIterator<TestConcreteDoc> iterator = mock(StorageIterator.class);
    when(iterator.getSome(anyInt())).thenReturn(limitedList);

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    when(storage.getAllByType(type)).thenReturn(iterator);

    List<TestConcreteDoc> actualList = instance.getAllLimited(type, 0, 3);

    assertEquals(3, actualList.size());
  }

  @Test
  public void testGetAllLimitedLimitIsZero() {
    List<TestConcreteDoc> documentList = instance.getAllLimited(TestConcreteDoc.class, 0, 0);

    assertTrue(documentList.isEmpty());
  }

}
