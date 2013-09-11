package nl.knaw.huygens.repository.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.managers.model.MultipleReferringDoc;
import nl.knaw.huygens.repository.managers.model.ReferredDoc;
import nl.knaw.huygens.repository.managers.model.ReferringDoc;
import nl.knaw.huygens.repository.messages.Broker;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.persistence.PersistenceException;
import nl.knaw.huygens.repository.persistence.PersistenceManager;
import nl.knaw.huygens.repository.persistence.PersistenceWrapper;
import nl.knaw.huygens.repository.variation.model.GeneralTestDoc;
import nl.knaw.huygens.repository.variation.model.TestConcreteDoc;
import nl.knaw.huygens.repository.variation.model.projecta.OtherDoc;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class StorageManagerTest {

  private StorageManager instance;
  private VariationStorage storage;
  private Set<String> documentTypes;
  private Broker broker;
  private DocTypeRegistry docTypeRegistry;
  private PersistenceWrapper persistenceWrapper;

  @Before
  public void SetUp() {
    storage = mock(VariationStorage.class);
    documentTypes = new HashSet<String>();
    broker = mock(Broker.class);
    docTypeRegistry = mock(DocTypeRegistry.class);
    persistenceWrapper = mock(PersistenceWrapper.class);
    instance = new StorageManager(storage, documentTypes, broker, docTypeRegistry, persistenceWrapper);
  }

  @Test
  public void testGetCompleteDocumentDocumentFound() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";

    GeneralTestDoc doc = mock(GeneralTestDoc.class);
    when(doc.getId()).thenReturn(id);
    when(doc.getDisplayName()).thenReturn("test");

    when(storage.getItem(type, id)).thenReturn(doc);

    Document actualDoc = instance.getCompleteDocument(type, id);

    assertEquals(id, actualDoc.getId());
    assertEquals("test", actualDoc.getDisplayName());
  }

  @Test
  public void testGetCompleteDocumentDocumentNotFound() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";

    when(storage.getItem(type, id)).thenReturn(null);

    assertNull(instance.getCompleteDocument(type, id));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetCompleteDocumentIOException() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";

    when(storage.getItem(type, id)).thenThrow(IOException.class);

    assertNull(instance.getCompleteDocument(type, id));
  }

  @Test
  public void testGetDocumentDocumentFound() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";

    GeneralTestDoc doc = mock(GeneralTestDoc.class);
    when(doc.getId()).thenReturn(id);
    when(doc.getDisplayName()).thenReturn("test");

    when(storage.getItem(type, id)).thenReturn(doc);

    Document actualDoc = instance.getDocument(type, id);

    assertEquals(id, actualDoc.getId());
    assertEquals("test", actualDoc.getDisplayName());
  }

  @Test
  public void testGetDocumentDocumentNotFound() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";

    when(storage.getItem(type, id)).thenReturn(null);

    assertNull(instance.getDocument(type, id));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetDocumentIOException() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";

    when(storage.getItem(type, id)).thenThrow(IOException.class);

    assertNull(instance.getDocument(type, id));
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

    Document actualDoc = instance.getCompleteVariation(type, id, variation);

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
  public void testAddDocumentDocumentAdded() throws IOException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    instance.addDocument(type, doc);

    verify(storage).addItem(type, doc);
  }

  @Test(expected = IOException.class)
  public void testAddDocumentStorageException() throws IOException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    doThrow(IOException.class).when(storage).addItem(type, doc);

    instance.addDocument(type, doc);
  }

  @Test
  public void testAddDocumentPersistentException() throws IOException, PersistenceException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    doThrow(PersistenceException.class).when(persistenceWrapper).persistObject(anyString(), anyString());

    instance.addDocument(type, doc);

    verify(storage).addItem(type, doc);
  }

  @Test
  public void testModifyDocumentDocumentModified() throws IOException {
    TestConcreteDoc expectedDoc = new TestConcreteDoc();
    expectedDoc.name = "test";
    expectedDoc.setId("TCD0000000001");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    instance.modifyDocument(type, expectedDoc);
    verify(storage).updateItem(type, expectedDoc.getId(), expectedDoc);
  }

  @Test(expected = IOException.class)
  public void testModifyDocumentStorageException() throws IOException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    doc.setId("TCD0000000001");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    doThrow(IOException.class).when(storage).updateItem(type, doc.getId(), doc);

    instance.modifyDocument(type, doc);
  }

  @Test
  public void testModifyDocumentPersistentException() throws IOException, PersistenceException {
    TestConcreteDoc expectedDoc = new TestConcreteDoc();
    expectedDoc.name = "test";
    expectedDoc.setId("TCD0000000001");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    instance.modifyDocument(type, expectedDoc);
    verify(storage).updateItem(type, expectedDoc.getId(), expectedDoc);
  }

  @Test
  public void testRemoveDocumentDocumentRemoved() throws IOException {
    TestConcreteDoc inputDoc = new TestConcreteDoc();
    inputDoc.name = "test";
    inputDoc.setId("TCD0000000001");
    inputDoc.setDeleted(true);

    Class<TestConcreteDoc> type = TestConcreteDoc.class;

    instance.removeDocument(type, inputDoc);
    verify(storage).deleteItem(type, inputDoc.getId(), inputDoc.getLastChange());
  }

  @Test(expected = IOException.class)
  public void testRemoveDocumentStorageException() throws IOException {
    TestConcreteDoc doc = new TestConcreteDoc();
    doc.name = "test";
    doc.setId("TCD0000000001");

    Class<TestConcreteDoc> type = TestConcreteDoc.class;
    doThrow(IOException.class).when(storage).deleteItem(type, doc.getId(), doc.getLastChange());

    instance.removeDocument(type, doc);
  }

  @Test
  public void testGetLastChanged() throws IOException {
    List<Document> lastChangeList = Lists.newArrayList(mock(Document.class), mock(Document.class), mock(Document.class));

    when(storage.getLastChanged(anyInt())).thenReturn(lastChangeList);

    List<Document> actualList = instance.getLastChanged(3);
    assertEquals(3, actualList.size());
  }

  @SuppressWarnings("unchecked")
  @Test()
  public void testGetLastChangedIOException() throws IOException {
    when(storage.getLastChanged(anyInt())).thenThrow(IOException.class);

    List<Document> actualList = instance.getLastChanged(3);
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

  @SuppressWarnings("unchecked")
  @Test
  public void testGetReferringDocs() {
    String referredDocId = "referreddoc";
    String referringDocId = "referringdoc";
    documentTypes.addAll(Sets.newHashSet(referringDocId, referredDocId));

    Class<ReferringDoc> referringDocType = ReferringDoc.class;
    Class<ReferredDoc> referredDocType = ReferredDoc.class;

    doReturn(referredDocType).when(docTypeRegistry).getTypeForIName(referredDocId);
    doReturn(referringDocType).when(docTypeRegistry).getTypeForIName(referringDocId);

    when(storage.getIdsForQuery(any(Class.class), any(List.class), any(String[].class))).thenReturn(Lists.newArrayList("RFD000000001"));

    instance = new StorageManager(storage, documentTypes, broker, docTypeRegistry, persistenceWrapper);

    Map<List<String>, List<String>> referringDocs = instance.getReferringDocs(referringDocType, referredDocType, "RDD000000001");

    assertFalse(referringDocs.isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetReferringDocsWithMultipleReferringDoc() {
    String referredDocId = "referreddoc";
    String referringDocId = "referringdoc";
    String multipleReferringDocId = "multiplereferringdoc";

    documentTypes.addAll(Sets.newHashSet(referringDocId, referredDocId, multipleReferringDocId));

    Class<ReferringDoc> referringDocType = ReferringDoc.class;
    Class<ReferredDoc> referredDocType = ReferredDoc.class;
    Class<MultipleReferringDoc> multipleReferringDocType = MultipleReferringDoc.class;

    doReturn(referredDocType).when(docTypeRegistry).getTypeForIName(referredDocId);
    doReturn(referringDocType).when(docTypeRegistry).getTypeForIName(referringDocId);
    doReturn(multipleReferringDocType).when(docTypeRegistry).getTypeForIName(multipleReferringDocId);

    when(storage.getIdsForQuery(any(Class.class), any(List.class), any(String[].class))).thenReturn(Lists.newArrayList("RFD000000001", "RDD000000001"));

    instance = new StorageManager(storage, documentTypes, broker, docTypeRegistry, persistenceWrapper);

    Map<List<String>, List<String>> referringDocs = instance.getReferringDocs(multipleReferringDocType, referredDocType, "RDD000000001");

    assertFalse(referringDocs.isEmpty());
  }

  @Test
  public void testGetReferringDocsNoMappingsForReferringType() {
    String referredDocId = "referreddoc";
    String referringDocId = "referringdoc";
    documentTypes.addAll(Sets.newHashSet(referredDocId, referringDocId));

    Class<ReferringDoc> referringDocType = ReferringDoc.class;
    Class<ReferredDoc> referredDocType = ReferredDoc.class;
    Class<OtherDoc> otherDocType = OtherDoc.class;

    doReturn(referredDocType).when(docTypeRegistry).getTypeForIName(referredDocId);
    doReturn(referringDocType).when(docTypeRegistry).getTypeForIName(referringDocId);

    instance = new StorageManager(storage, documentTypes, broker, docTypeRegistry, persistenceWrapper);

    Map<List<String>, List<String>> referringDocs = instance.getReferringDocs(otherDocType, referredDocType, "RDD000000001");

    assertTrue(referringDocs.isEmpty());
  }

  @Test
  public void testGetReferringDocsNoMappingsForReferredType() {
    String referredDocId = "referreddoc";
    String referringDocId = "referringdoc";
    documentTypes.addAll(Sets.newHashSet(referredDocId, referringDocId));

    Class<ReferringDoc> referringDocType = ReferringDoc.class;
    Class<ReferredDoc> referredDocType = ReferredDoc.class;
    Class<OtherDoc> otherDocType = OtherDoc.class;

    doReturn(referredDocType).when(docTypeRegistry).getTypeForIName(referredDocId);
    doReturn(referringDocType).when(docTypeRegistry).getTypeForIName(referringDocId);

    instance = new StorageManager(storage, documentTypes, broker, docTypeRegistry, persistenceWrapper);

    Map<List<String>, List<String>> referringDocs = instance.getReferringDocs(referringDocType, otherDocType, "RDD000000001");

    assertTrue(referringDocs.isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetReferringDocsReferringDocsNotFound() {
    String referredDocId = "referreddoc";
    String referringDocId = "referringdoc";
    documentTypes.addAll(Sets.newHashSet(referringDocId, referredDocId));

    Class<ReferringDoc> referringDocType = ReferringDoc.class;
    Class<ReferredDoc> referredDocType = ReferredDoc.class;

    doReturn(referredDocType).when(docTypeRegistry).getTypeForIName(referredDocId);
    doReturn(referringDocType).when(docTypeRegistry).getTypeForIName(referringDocId);

    when(storage.getIdsForQuery(any(Class.class), any(List.class), any(String[].class))).thenReturn(Lists.<ReferringDoc> newArrayList());

    instance = new StorageManager(storage, documentTypes, broker, docTypeRegistry, persistenceWrapper);

    Map<List<String>, List<String>> referringDocs = instance.getReferringDocs(referringDocType, referredDocType, "RDD000000001");

    assertTrue(referringDocs.isEmpty());
  }

}
