package nl.knaw.huygens.timbuctoo.index.request;

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class IndexRequestFactory {
  private final Repository repository;

  @Inject
  public IndexRequestFactory(Repository repository) {
    this.repository = repository;
  }

  public IndexRequest forCollectionOf(Class<? extends DomainEntity> type) {
    return new CollectionIndexRequest(type, repository);
  }

  public IndexRequest forEntity(Class<? extends DomainEntity> type, String id) {
    return new EntityIndexRequest(type, id);
  }
}
