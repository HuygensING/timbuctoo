package nl.knaw.huygens.timbuctoo.index;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.solr.common.SolrInputDocument;

/**
 * A class an input document for the types in the scope.
 *
 */
public class SolrInputDocumentCreator {

  private final ModelIterator modelIterator;

  public SolrInputDocumentCreator(ModelIterator modelIterator) {
    this.modelIterator = modelIterator;
  }

  public SolrInputDocumentCreator() {
    this(new ModelIterator());
  }

  public <T extends DomainEntity> SolrInputDocument create(List<T> entities) {
    SolrInputDocument document = null;
    for (T entity : entities) {
      SolrInputDocGenerator docGenerator = createSolerInputDocGenerator(entity, document);
      modelIterator.processClass(docGenerator, entity.getClass());
      document = docGenerator.getResult();
    }
    return document;

  }

  protected <T extends DomainEntity> SolrInputDocGenerator createSolerInputDocGenerator(T entity, SolrInputDocument document) {
    SolrInputDocGenerator indexer = null;

    if (document == null) {
      indexer = new SolrInputDocGenerator(entity);
    } else {
      indexer = new SolrInputDocGenerator(entity, document);
    }
    return indexer;
  }

}
