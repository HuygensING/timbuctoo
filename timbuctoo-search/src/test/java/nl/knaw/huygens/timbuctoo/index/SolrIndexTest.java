package nl.knaw.huygens.timbuctoo.index;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SolrIndexTest {
  @Mock
  private List<? extends DomainEntity> variationsToAdd;
  private AbstractSolrServer solrServerMock;
  private SolrInputDocument solrInputDocumentMock;
  private SolrInputDocumentCreator documentCreatorMock;
  private SolrIndex instance;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    solrServerMock = mock(AbstractSolrServer.class);
    solrInputDocumentMock = mock(SolrInputDocument.class);
    documentCreatorMock = mock(SolrInputDocumentCreator.class);

    instance = new SolrIndex(documentCreatorMock, solrServerMock);
  }

  @Test
  public void testAdd() throws SolrServerException, IOException, IndexException {
    // when
    when(documentCreatorMock.create(variationsToAdd)).thenReturn(solrInputDocumentMock);

    // action
    instance.add(variationsToAdd);

    // verify
    InOrder inOrder = Mockito.inOrder(documentCreatorMock, solrServerMock);
    inOrder.verify(documentCreatorMock).create(variationsToAdd);
    inOrder.verify(solrServerMock).add(solrInputDocumentMock);

  }

  @Test(expected = IndexException.class)
  public void testAddWhenSolrServerThrowsASolrServerException() throws SolrServerException, IOException, IndexException {
    testAddWhenSolrServerThrowsAnException(SolrServerException.class);
  }

  @Test(expected = IndexException.class)
  public void testAddWhenSolrServerThrowsAnIOException() throws SolrServerException, IOException, IndexException {
    testAddWhenSolrServerThrowsAnException(IOException.class);
  }

  private void testAddWhenSolrServerThrowsAnException(Class<? extends Exception> exceptionToThrow) throws SolrServerException, IOException, IndexException {
    // when
    when(documentCreatorMock.create(variationsToAdd)).thenReturn(solrInputDocumentMock);
    doThrow(exceptionToThrow).when(solrServerMock).add(solrInputDocumentMock);

    // action
    try {
      instance.add(variationsToAdd);
    } finally {
      // verify
      InOrder inOrder = Mockito.inOrder(documentCreatorMock, solrServerMock);
      inOrder.verify(documentCreatorMock).create(variationsToAdd);
      inOrder.verify(solrServerMock).add(solrInputDocumentMock);
    }
  }

  @Test
  public void testUpdate() throws IndexException, SolrServerException, IOException {
    // when
    when(documentCreatorMock.create(variationsToAdd)).thenReturn(solrInputDocumentMock);

    // action
    instance.update(variationsToAdd);

    // verify
    InOrder inOrder = Mockito.inOrder(documentCreatorMock, solrServerMock);
    inOrder.verify(documentCreatorMock).create(variationsToAdd);
    inOrder.verify(solrServerMock).add(solrInputDocumentMock);
  }

}
