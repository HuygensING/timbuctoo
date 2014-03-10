package nl.knaw.huygens.timbuctoo.index;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class IndexFacadeTest {
  @Test
  public void testAddEntity() throws SolrServerException, IOException, IndexException {
    // mock
    AbstractSolrServer abstractSolrServerMock = mock(AbstractSolrServer.class);
    StorageManager storageManagerMock = mock(StorageManager.class);

    IndexFacade instance = new IndexFacade();
    String id = "id0000001";
    Class<ExplicitlyAnnotatedModel> type = ExplicitlyAnnotatedModel.class;

    // action
    instance.addEntity(type, id);

    // verify
    InOrder inOrder = Mockito.inOrder(storageManagerMock, abstractSolrServerMock);
    inOrder.verify(storageManagerMock).getAllVariations(type, id);
    inOrder.verify(abstractSolrServerMock).add(any(SolrInputDocument.class));
  }

  @Test
  public void testAddEntityWhenSolrServerThrowsAnException() {
    fail("Yet to be implemented");
  }
}
