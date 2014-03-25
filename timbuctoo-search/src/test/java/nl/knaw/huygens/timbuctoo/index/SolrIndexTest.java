package nl.knaw.huygens.timbuctoo.index;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

import com.google.common.collect.Lists;

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
    verifyTheIndexIsUpdated();

  }

  private void verifyTheIndexIsUpdated() throws SolrServerException, IOException {
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
      verifyTheIndexIsUpdated();
    }
  }

  @Test
  public void testUpdate() throws IndexException, SolrServerException, IOException {
    // when
    when(documentCreatorMock.create(variationsToAdd)).thenReturn(solrInputDocumentMock);

    // action
    instance.update(variationsToAdd);

    // verify
    verifyTheIndexIsUpdated();
  }

  @Test
  public void testDelete() throws SolrServerException, IOException, IndexException {
    String id = "ID";
    // action
    instance.deleteById(id);

    // verify
    verify(solrServerMock).deleteById(id);
  }

  @Test(expected = IndexException.class)
  public void testDeleteSolrServerThrowsSolrServerException() throws SolrServerException, IOException, IndexException {
    testDeleteSolrServerThrowsException(SolrServerException.class);
  }

  @Test(expected = IndexException.class)
  public void testDeleteSolrServerThrowsIOException() throws SolrServerException, IOException, IndexException {
    testDeleteSolrServerThrowsException(IOException.class);
  }

  private void testDeleteSolrServerThrowsException(Class<? extends Exception> exceptionToThrow) throws SolrServerException, IOException, IndexException {
    String id = "ID";

    // when
    doThrow(exceptionToThrow).when(solrServerMock).deleteById(id);

    try {
      // action
      instance.deleteById(id);
    } finally {
      // verify
      verify(solrServerMock).deleteById(id);
    }
  }

  @Test
  public void testDeleteMultipleItems() throws SolrServerException, IOException, IndexException {
    // setup
    List<String> ids = Lists.newArrayList("id1", "id2", "id3");

    // action
    instance.deleteById(ids);

    // verify
    verify(solrServerMock).deleteById(ids);
  }

  @Test(expected = IndexException.class)
  public void testDeleteMultipleItemsSolrServerThrowsIOException() throws SolrServerException, IOException, IndexException {
    testDeleteMultipleItemsSolrServerThrowsException(IOException.class);
  }

  @Test(expected = IndexException.class)
  public void testDeleteMultipleItemsSolrServerThrowsSolrServerException() throws SolrServerException, IOException, IndexException {
    testDeleteMultipleItemsSolrServerThrowsException(SolrServerException.class);
  }

  private void testDeleteMultipleItemsSolrServerThrowsException(Class<? extends Exception> exceptionToThrow) throws SolrServerException, IOException, IndexException {
    List<String> ids = Lists.newArrayList("id1", "id2", "id3");

    // when
    doThrow(exceptionToThrow).when(solrServerMock).deleteById(ids);

    try {
      // action
      instance.deleteById(ids);
    } finally {
      // verify
      verify(solrServerMock).deleteById(ids);
    }
  }

}
