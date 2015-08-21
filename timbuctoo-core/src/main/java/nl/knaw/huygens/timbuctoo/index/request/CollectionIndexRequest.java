package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import java.time.LocalDateTime;

class CollectionIndexRequest extends AbstractIndexRequest {
  private final Class<? extends DomainEntity> type;
  private final Repository repository;
  private LocalDateTime lastChanged;
  private Status status;

  public CollectionIndexRequest(Class<? extends DomainEntity> type, Repository repository) {
    super();
    this.repository = repository;
    this.type = type;
  }

  @Override
  protected String getDesc() {
    return String.format("Index request for [%s]", TypeNames.getExternalName(type));
  }

  @Override
  public void execute(Indexer indexer) throws IndexException {
    for (StorageIterator<? extends DomainEntity> iterator = repository.getDomainEntities(type); iterator.hasNext(); ) {
      indexer.executeIndexAction(type, iterator.next().getId());
    }
  }
}
