package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class CollectionIndexRequest extends AbstractIndexRequest {
  private final Repository repository;
  private final RequestItemStatus requestItemStatus;

  public CollectionIndexRequest(Class<? extends DomainEntity> type, Repository repository) {
    this(type, repository, IndexRequestStatus.requested(), new RequestItemStatus());
  }

  CollectionIndexRequest(Class<? extends DomainEntity> type, Repository repository, IndexRequestStatus indexRequestStatus, RequestItemStatus requestItemStatus) {
    super(type, indexRequestStatus);
    this.repository = repository;
    this.requestItemStatus = requestItemStatus;
  }

  @Override
  protected String getDesc() {
    return String.format("Index request for [%s]", TypeNames.getExternalName(getType()));
  }

  @Override
  protected void executeIndexAction(Indexer indexer) throws IndexException {
    Class<? extends DomainEntity> type = getType();

    Spliterator<? extends DomainEntity> spliterator = Spliterators.spliteratorUnknownSize(repository.getDomainEntities(type), Spliterator.DISTINCT);
    this.requestItemStatus.setToDo(StreamSupport.stream(spliterator, false).map(entity -> entity.getId()).collect(Collectors.toList()));

    for (String id : requestItemStatus.getToDo()) {
      indexer.executeIndexAction(type, id);
      requestItemStatus.done(id);
    }
  }

}
