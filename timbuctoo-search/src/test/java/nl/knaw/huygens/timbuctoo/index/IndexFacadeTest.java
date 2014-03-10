package nl.knaw.huygens.timbuctoo.index;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.index.model.SubModel;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

public class IndexFacadeTest {
  private AbstractSolrServer abstractSolrServerMock;
  private SolrInputDocumentCreator solrInputDocCreatorMock;
  private SolrInputDocument solrInputDocumentMock;
  private StorageManager storageManagerMock;
  private TypeRegistry typeRegistryMock;

  private IndexFacade instance;

  @Before
  public void setUp() {
    abstractSolrServerMock = mock(AbstractSolrServer.class);
    solrInputDocCreatorMock = mock(SolrInputDocumentCreator.class);
    solrInputDocumentMock = mock(SolrInputDocument.class);
    storageManagerMock = mock(StorageManager.class);
    typeRegistryMock = mock(TypeRegistry.class);

    instance = new IndexFacade(abstractSolrServerMock, solrInputDocCreatorMock, storageManagerMock, typeRegistryMock);
  }

  @Test
  public void testAddEntity() throws SolrServerException, IOException, IndexException {
    //when
    doReturn(ExplicitlyAnnotatedModel.class).when(typeRegistryMock).getBaseClass(SubModel.class);
    List<ExplicitlyAnnotatedModel> allVariations = Lists.newArrayList();
    when(storageManagerMock.getAllVariations(ExplicitlyAnnotatedModel.class, "id0000001")).thenReturn(allVariations);
    when(solrInputDocCreatorMock.create(allVariations, "id0000001")).thenReturn(solrInputDocumentMock);

    // action
    instance.addEntity(SubModel.class, "id0000001");

    // verify
    InOrder inOrder = Mockito.inOrder(typeRegistryMock, storageManagerMock, solrInputDocCreatorMock, abstractSolrServerMock);
    inOrder.verify(typeRegistryMock).getBaseClass(SubModel.class);
    inOrder.verify(storageManagerMock).getAllVariations(ExplicitlyAnnotatedModel.class, "id0000001");
    inOrder.verify(solrInputDocCreatorMock).create(allVariations, "id0000001");
    inOrder.verify(abstractSolrServerMock).add(solrInputDocumentMock);
  }

  @Test(expected = IndexException.class)
  public void testAddEntityWhenSolrServerThrowsAnIOException() throws SolrServerException, IOException, IndexException {
    testAddEntityWhenSolrServerThrowsAnException(IOException.class);
  }

  @Test(expected = IndexException.class)
  public void testAddEntityWhenSolrServerThrowsASolrServerException() throws SolrServerException, IOException, IndexException {
    testAddEntityWhenSolrServerThrowsAnException(SolrServerException.class);
  }

  private void testAddEntityWhenSolrServerThrowsAnException(Class<? extends Exception> exception) throws SolrServerException, IOException, IndexException {
    //when
    doReturn(ExplicitlyAnnotatedModel.class).when(typeRegistryMock).getBaseClass(SubModel.class);
    List<ExplicitlyAnnotatedModel> allVariations = Lists.newArrayList();
    when(storageManagerMock.getAllVariations(ExplicitlyAnnotatedModel.class, "id0000001")).thenReturn(allVariations);
    when(solrInputDocCreatorMock.create(allVariations, "id0000001")).thenReturn(solrInputDocumentMock);
    doThrow(exception).when(abstractSolrServerMock).add(solrInputDocumentMock);

    // action
    try {
      instance.addEntity(SubModel.class, "id0000001");
    } finally {
      // verify
      InOrder inOrder = Mockito.inOrder(typeRegistryMock, storageManagerMock, solrInputDocCreatorMock, abstractSolrServerMock);
      inOrder.verify(typeRegistryMock).getBaseClass(SubModel.class);
      inOrder.verify(storageManagerMock).getAllVariations(ExplicitlyAnnotatedModel.class, "id0000001");
      inOrder.verify(solrInputDocCreatorMock).create(allVariations, "id0000001");
      inOrder.verify(abstractSolrServerMock).add(solrInputDocumentMock);
    }
  }
}
