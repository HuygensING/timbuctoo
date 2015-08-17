package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

class AddIndexer extends AbstractIndexer {

  public AddIndexer(Repository repository, IndexManager indexManager) {
    super(repository, indexManager);
  }

  protected void executeIndexAction(Class<? extends DomainEntity> type, String id) throws IndexException {
    getIndexManager().addEntity(type, id);
  }
}
