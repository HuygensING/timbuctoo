package nl.knaw.huygens.repository.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jms.JMSException;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.messages.ActionType;
import nl.knaw.huygens.repository.messages.Broker;
import nl.knaw.huygens.repository.messages.Producer;
import nl.knaw.huygens.repository.model.DomainEntity;
import nl.knaw.huygens.repository.model.Entity;
import nl.knaw.huygens.repository.persistence.PersistenceWrapper;
import nl.knaw.huygens.repository.storage.mongo.model.TestSystemDocument;
import nl.knaw.huygens.repository.variation.model.GeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import com.google.common.collect.Lists;

public class StorageManagerTest {

  private StorageManager instance;
  private VariationStorage storage;
  private Set<String> documentTypes;
  private Broker broker;
  private DocTypeRegistry docTypeRegistry;
  private PersistenceWrapper persistenceWrapper;
  private Producer producer;

  @Before
  public void SetUp() throws JMSException {
    storage = mock(VariationStorage.class);
    documentTypes = new HashSet<String>();
    broker = mock(Broker.class);
    producer = mock(Producer.class);
    when(broker.newProducer(anyString(), anyString())).thenReturn(producer);
    docTypeRegistry = mock(DocTypeRegistry.class);
    persistenceWrapper = mock(PersistenceWrapper.class);
    instance = new StorageManager(storage, documentTypes, broker, docTypeRegistry, persistenceWrapper);
  }

  @Test
  public void testAddDocumentDomainDocument() throws IOException, PersistenceException, JMSException {
    String id = "GTD000000001";
    GeneralTestDoc doc = new GeneralTestDoc(id);
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    when(docTypeRegistry.getINameForType(type)).thenReturn("generaltestdoc");

    instance.addEntity(type, doc);

    verifyAddDocument(type, doc, times(1), times(1), times(1));
  }

  @Test
  public void testAddDocumentDomainDocumentInComplete() throws JMSException, PersistenceException, IOException {
    String id = "GTD000000001";
    GeneralTestDoc doc = new GeneralTestDoc(id);
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    when(docTypeRegistry.getINameForType(type)).thenReturn("generaltestdoc");

    instance.addEntity(type, doc, false);

    verifyAddDocument(type, doc, times(1), times(1), never());
  }

  @Test
  public void testAddDocumentSystemDocument() throws IOException, PersistenceException, JMSException {
    TestSystemDocument doc = new TestSystemDocument();
    doc.setId("TSD0000000001");

    Class<TestSystemDocument> type = TestSystemDocument.class;

    instance.addEntity(type, doc);

    verifyAddDocument(type, doc, times(1), never(), never());
  }

  @Test(expected = IOException.class)
  public void testAddDocumentStorageException() throws IOException, PersistenceException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    doThrow(IOException.class).when(storage).addItem(type, doc);

    instance.addEntity(type, doc);
  }

  @Test
  public void testAddDocumentPersistentException() throws IOException, PersistenceException, JMSException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    doThrow(PersistenceException.class).when(persistenceWrapper).persistObject(anyString(), anyString());

    instance.addEntity(type, doc);

    verifyAddDocument(type, doc, times(1), times(1), times(1));
  }

  protected <T extends Entity> void verifyAddDocument(Class<T> type, T doc, VerificationMode storageVerification, VerificationMode persistenceVerification, VerificationMode indexingVerification)
      throws IOException, PersistenceException, JMSException {

    verify(storage, storageVerification).addItem(type, doc);
    verify(persistenceWrapper, persistenceVerification).persistObject(anyString(), anyString());
    verify(producer, indexingVerification).send(any(ActionType.class), anyString(), anyString());
  }

  @Test
  public void testAddDocumentWithoutPersistingCompleteDocument() throws IOException, PersistenceException, JMSException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    instance.addEntityWithoutPersisting(type, doc, true);

    verifyAddDocument(type, doc, times(1), never(), times(1));
  }

  @Test
  public void testAddDocumentWithoutPersistingInCompleteDocument() throws IOException, PersistenceException, JMSException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    instance.addEntityWithoutPersisting(type, doc, false);

    verifyAddDocument(type, doc, times(1), never(), never());
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

  @SuppressWarnings("unchecked")
  @Test
  public void testGetDocumentIOException() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";

    when(storage.getItem(type, id)).thenThrow(IOException.class);

    assertNull(instance.getEntity(type, id));
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
  public void testModifyDocumentDomainDocumentModified() throws IOException, PersistenceException, JMSException {
    TestConcreteDoc expectedDoc = new TestConcreteDoc();
    expectedDoc.name = "test";
    String id = "TCD0000000001";
    expectedDoc.setId(id);

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    instance.modifyEntity(type, expectedDoc);
    verifyModifyDocument(type, expectedDoc, times(1), times(1), times(1));
  }

  @Test
  public void testModifyDocumentSystemDocumentModified() throws IOException, PersistenceException, JMSException {
    String id = "TSD0000000001";
    TestSystemDocument expectedDoc = new TestSystemDocument();
    expectedDoc.setId(id);

    Class<TestSystemDocument> type = TestSystemDocument.class;

    instance.modifyEntity(type, expectedDoc);
    verifyModifyDocument(type, expectedDoc, times(1), never(), never());
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
  public void testModifyDocumentPersistentException() throws IOException, PersistenceException, JMSException {
    TestConcreteDoc expectedDoc = new TestConcreteDoc();
    expectedDoc.name = "test";
    expectedDoc.setId("TCD0000000001");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    instance.modifyEntity(type, expectedDoc);
    verifyModifyDocument(type, expectedDoc, times(1), times(1), times(1));
  }

  protected <T extends Entity> void verifyModifyDocument(Class<T> type, T expectedDoc, VerificationMode storageVerification, VerificationMode persistenceVerification,
      VerificationMode indexVerification) throws IOException, PersistenceException, JMSException {

    verify(storage, storageVerification).updateItem(type, expectedDoc.getId(), expectedDoc);
    verify(persistenceWrapper, persistenceVerification).persistObject(anyString(), anyString());
    verify(producer, indexVerification).send(any(ActionType.class), anyString(), anyString());
  }

  @Test
  public void testModifyDocumentWithoutPersisitingDomainDocument() throws IOException, PersistenceException, JMSException {
    TestConcreteDoc expectedDoc = new TestConcreteDoc();
    expectedDoc.name = "test";
    String id = "TCD0000000001";
    expectedDoc.setId(id);

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    instance.modifyEntityWithoutPersisting(type, expectedDoc);
    verifyModifyDocument(type, expectedDoc, times(1), never(), times(1));
  }

  @Test
  public void testRemoveDocumentDomainDocumentRemoved() throws IOException, JMSException {
    TestConcreteDoc inputDoc = new TestConcreteDoc();
    inputDoc.name = "test";
    String id = "TCD0000000001";
    inputDoc.setId(id);
    inputDoc.setDeleted(true);

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    String typeString = "testconcretedoc";

    when(docTypeRegistry.getINameForType(Mockito.<Class<? extends Entity>> any())).thenReturn(typeString);

    instance.removeEntity(type, inputDoc);
    verify(storage).deleteItem(type, inputDoc.getId(), inputDoc.getLastChange());
    verify(producer).send(ActionType.INDEX_DEL, typeString, id);
  }

  @Test
  public void testRemoveDocumentSystemDocumentRemoved() throws IOException, JMSException {
    TestSystemDocument inputDoc = new TestSystemDocument();
    String id = "TCD0000000001";
    inputDoc.setId(id);
    inputDoc.setDeleted(true);

    Class<TestSystemDocument> type = TestSystemDocument.class;

    instance.removeEntity(type, inputDoc);
    verify(storage, times(1)).deleteItem(type, inputDoc.getId(), inputDoc.getLastChange());
    verify(producer, never()).send(any(ActionType.class), anyString(), anyString());
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
  public void testRemovePermanently() throws JMSException, IOException {
    String id1 = "PER0000000001";
    String id2 = "PER0000000002";
    String id3 = "PER0000000005";
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String typeString = "generaltestdoc";

    ArrayList<String> ids = Lists.newArrayList(id1, id2, id3);
    when(storage.getAllIdsWithoutPIDOfType(type)).thenReturn(ids);
    when(docTypeRegistry.getINameForType(type)).thenReturn(typeString);

    instance.removePermanently(type);

    ActionType action = ActionType.INDEX_DEL;
    verify(producer, times(1)).send(action, typeString, id1);
    verify(producer, times(1)).send(action, typeString, id2);
    verify(producer, times(1)).send(action, typeString, id3);

    verify(storage, times(1)).removePermanently(type, ids);

    verify(producer, never()).send(ActionType.INDEX_ADD, typeString, id1);
    verify(producer, never()).send(ActionType.INDEX_ADD, typeString, id2);
    verify(producer, never()).send(ActionType.INDEX_ADD, typeString, id3);
  }

  @Test(expected = RuntimeException.class)
  public void testRemovePermanentlyJMSException() throws JMSException, IOException {
    String id1 = "PER0000000001";
    String id2 = "PER0000000002";
    String id3 = "PER0000000005";
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String typeString = "generaltestdoc";

    ArrayList<String> ids = Lists.newArrayList(id1, id2, id3);
    when(storage.getAllIdsWithoutPIDOfType(type)).thenReturn(ids);
    when(docTypeRegistry.getINameForType(type)).thenReturn(typeString);
    doThrow(JMSException.class).when(producer).send(any(ActionType.class), anyString(), anyString());

    try {
      instance.removePermanently(type);
    } finally {
      verify(producer, times(1)).send(ActionType.INDEX_DEL, typeString, id1);
      verify(storage, never()).removePermanently(type, ids);
      verify(producer, never()).send(ActionType.INDEX_ADD, typeString, id1);
    }

  }

  @Test(expected = IOException.class)
  public void testRemovePermanentlyStorageDeleteException() throws JMSException, IOException {
    String id1 = "PER0000000001";
    String id2 = "PER0000000002";
    String id3 = "PER0000000005";
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String typeString = "generaltestdoc";

    ArrayList<String> ids = Lists.newArrayList(id1, id2, id3);

    when(storage.getAllIdsWithoutPIDOfType(type)).thenReturn(ids);
    doThrow(IOException.class).when(storage).removePermanently(type, ids);
    when(docTypeRegistry.getINameForType(type)).thenReturn(typeString);

    try {
      instance.removePermanently(type);
    } finally {
      ActionType delete = ActionType.INDEX_DEL;
      verify(producer, times(1)).send(delete, typeString, id1);
      verify(producer, times(1)).send(delete, typeString, id2);
      verify(producer, times(1)).send(delete, typeString, id3);

      verify(storage, times(1)).removePermanently(type, ids);

      // Verify the roll back
      ActionType add = ActionType.INDEX_ADD;
      verify(producer, times(1)).send(add, typeString, id1);
      verify(producer, times(1)).send(add, typeString, id2);
      verify(producer, times(1)).send(add, typeString, id3);
    }
  }

  @Test(expected = IOException.class)
  public void testRemovePermanentlyStorageGetException() throws JMSException, IOException {
    doThrow(IOException.class).when(storage).getAllIdsWithoutPIDOfType(GeneralTestDoc.class);

    try {
      instance.removePermanently(GeneralTestDoc.class);
    } finally {
      verify(producer, never()).send(any(ActionType.class), anyString(), anyString());
      verify(storage, never()).removePermanently(Mockito.<Class<? extends DomainEntity>> any(), Mockito.<List<String>> any());
    }
  }

  @Test
  public void testGetLastChanged() throws IOException {
    List<Entity> lastChangeList = Lists.newArrayList(mock(Entity.class), mock(Entity.class), mock(Entity.class));

    when(storage.getLastChanged(anyInt())).thenReturn(lastChangeList);

    List<Entity> actualList = instance.getLastChanged(3);
    assertEquals(3, actualList.size());
  }

  @SuppressWarnings("unchecked")
  @Test()
  public void testGetLastChangedIOException() throws IOException {
    when(storage.getLastChanged(anyInt())).thenThrow(IOException.class);

    List<Entity> actualList = instance.getLastChanged(3);
    assertTrue(actualList.isEmpty());
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
