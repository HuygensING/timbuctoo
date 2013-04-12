package nl.knaw.huygens.repository.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.variation.model.GeneralTestDoc;

import org.junit.Before;
import org.junit.Test;

public class StorageManagerTest {
  private StorageManager instance;
  private Storage storage;
  private Set<String> documentTypes;
  private Hub hub;
  private DocumentTypeRegister docTypeRegistry;

  @Before
  public void SetUp() {
    storage = mock(Storage.class);
    documentTypes = new HashSet<String>();
    hub = mock(Hub.class);
    docTypeRegistry = mock(DocumentTypeRegister.class);
    instance = new StorageManager(storage, documentTypes, hub, docTypeRegistry);
  }

  @Test
  public void testGetCompleteVariationDocumentFound() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";
    String variation = "projecta";

    GeneralTestDoc doc = mock(GeneralTestDoc.class);
    when(doc.getId()).thenReturn(id);
    when(doc.getDescription()).thenReturn("test");

    when(storage.getVariation(type, id, variation)).thenReturn(doc);

    Document actualDoc = instance.getCompleteVariation(type, id, variation);

    assertEquals(id, actualDoc.getId());
    assertEquals("test", actualDoc.getDescription());
  }

  @Test
  public void testGetCompleteVariationDocumentNotFound() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";
    String variation = "projecta";

    when(storage.getVariation(type, id, variation)).thenReturn(null);

    Document actualDoc = instance.getCompleteVariation(type, id, variation);

    assertNull(actualDoc);

  }

  @SuppressWarnings("unchecked")
  @Test()
  public void testGetCompleteVariationIOException() throws IOException {
    Class<GeneralTestDoc> type = GeneralTestDoc.class;
    String id = "testId";
    String variation = "projecta";

    when(storage.getVariation(type, id, variation)).thenThrow(IOException.class);

    Document actualDoc = instance.getCompleteVariation(type, id, variation);

    assertNull(actualDoc);
  }
}
