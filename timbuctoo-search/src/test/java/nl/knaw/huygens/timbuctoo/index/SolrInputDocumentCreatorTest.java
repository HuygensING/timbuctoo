package nl.knaw.huygens.timbuctoo.index;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.index.model.SubModel;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import com.google.common.collect.Lists;

public class SolrInputDocumentCreatorTest {
  @Test
  public void testCreateForOneEntity() {
    // mock
    final SolrInputDocGenerator solrInputDocGenerator = mock(SolrInputDocGenerator.class);
    ModelIterator modelIteratorMock = mock(ModelIterator.class);

    SolrInputDocumentCreator instance = new SolrInputDocumentCreator(modelIteratorMock) {
      @Override
      protected <T extends DomainEntity> SolrInputDocGenerator createSolerInputDocGenerator(T entity, SolrInputDocument doc) {
        return solrInputDocGenerator;
      }
    };

    List<DomainEntity> variationsToAdd = Lists.newArrayList();
    variationsToAdd.add(new ExplicitlyAnnotatedModel());

    // action
    instance.create(variationsToAdd);

    // verify
    verify(modelIteratorMock).processClass(solrInputDocGenerator, ExplicitlyAnnotatedModel.class);
  }

  @Test
  public void testCreateForMultipleEntities() {
    // mock
    final SolrInputDocGenerator solrInputDocGenerator = mock(SolrInputDocGenerator.class);
    ModelIterator modelIteratorMock = mock(ModelIterator.class);

    SolrInputDocumentCreator instance = new SolrInputDocumentCreator(modelIteratorMock) {
      @Override
      protected <T extends DomainEntity> SolrInputDocGenerator createSolerInputDocGenerator(T entity, SolrInputDocument doc) {
        return solrInputDocGenerator;
      }
    };

    List<DomainEntity> variationsToAdd = Lists.newArrayList();
    variationsToAdd.add(new ExplicitlyAnnotatedModel());
    variationsToAdd.add(new SubModel());

    // action
    instance.create(variationsToAdd);

    // verify
    verify(modelIteratorMock).processClass(solrInputDocGenerator, ExplicitlyAnnotatedModel.class);
    verify(modelIteratorMock).processClass(solrInputDocGenerator, SubModel.class);
  }

}
