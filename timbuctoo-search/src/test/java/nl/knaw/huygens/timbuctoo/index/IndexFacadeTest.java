package nl.knaw.huygens.timbuctoo.index;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.timbuctoo.index.model.SubModel;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class IndexFacadeTest {
  private AbstractSolrServer abstractSolrServerMock;
  private SolrInputDocumentCreator solrInputDocCreatorMock;
  private SolrInputDocument solrInputDocumentMock;

  private IndexFacade instance;

  @Before
  public void setUp() {
    abstractSolrServerMock = mock(AbstractSolrServer.class);
    solrInputDocCreatorMock = mock(SolrInputDocumentCreator.class);
    solrInputDocumentMock = mock(SolrInputDocument.class);

    instance = new IndexFacade(abstractSolrServerMock, solrInputDocCreatorMock);
  }

  @Test
  public void testAddEntity() throws SolrServerException, IOException, IndexException {
    //when
    when(solrInputDocCreatorMock.create(SubModel.class, "id0000001")).thenReturn(solrInputDocumentMock);

    // action
    instance.addEntity(SubModel.class, "id0000001");

    // verify
    InOrder inOrder = Mockito.inOrder(solrInputDocCreatorMock, abstractSolrServerMock);
    inOrder.verify(solrInputDocCreatorMock).create(SubModel.class, "id0000001");
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
    when(solrInputDocCreatorMock.create(SubModel.class, "id0000001")).thenReturn(solrInputDocumentMock);
    doThrow(exception).when(abstractSolrServerMock).add(solrInputDocumentMock);

    // action
    try {
      instance.addEntity(SubModel.class, "id0000001");
    } finally {
      // verify
      InOrder inOrder = Mockito.inOrder(solrInputDocCreatorMock, abstractSolrServerMock);
      inOrder.verify(solrInputDocCreatorMock).create(SubModel.class, "id0000001");
      inOrder.verify(abstractSolrServerMock).add(solrInputDocumentMock);
    }
  }
}
