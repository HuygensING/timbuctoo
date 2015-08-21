package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;

import java.time.LocalDateTime;

public class IndexRequestImpl implements IndexRequest {

  private String desc;
  private Class<? extends DomainEntity> type;
  private Status status;
  private LocalDateTime lastChanged;
  private String id;

  private IndexRequestImpl() {
    status = Status.REQUESTED;
    lastChanged = LocalDateTime.now();
  }

  public static IndexRequestImpl forEntity(Class<? extends DomainEntity> type, String id) {
    IndexRequestImpl indexRequest = new IndexRequestImpl();
    indexRequest.setId(id);
    indexRequest.setType(type);
    indexRequest.setDesc(String.format("Index request for [%s] with id [%s]", TypeNames.getExternalName(type), id));
    return indexRequest;
  }

  public static IndexRequestImpl forType(Class<? extends DomainEntity> type) {
    IndexRequestImpl indexRequest = new IndexRequestImpl();
    indexRequest.setType(type);
    indexRequest.setDesc(String.format("Index request for [%s]", TypeNames.getExternalName(type)));
    return indexRequest;
  }

  @Override
  public void setDesc(String desc) {
    this.desc = desc;
  }

  @Override
  public String getDesc() {
    return desc;
  }

  @Override
  public String toClientRep() {
    return String.format("{\"desc\":\"%s\", \"status\":\"%s\"}", desc, status);
  }

  @Override
  public Class<? extends DomainEntity> getType() {
    return type;
  }

  @Override
  public void setType(Class<? extends DomainEntity> type) {
    this.type = type;
  }

  @Override
  public Status getStatus() {
    return status;
  }

  @Override
  public void inProgress() {
    status = Status.IN_PROGRESS;
    lastChanged = LocalDateTime.now();
  }

  @Override
  public void done() {
    status = Status.DONE;
    lastChanged = LocalDateTime.now();
  }

  @Override
  public LocalDateTime getLastChanged() {
    return lastChanged;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public StorageIterator<? extends DomainEntity> getEntities(Repository repository) {
    if(id != null){
      return StorageIteratorStub.newInstance(repository.getEntityOrDefaultVariation(type, id));
    }

    return repository.getDomainEntities(type);
  }

  @Override
  public void execute(Indexer indexer) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
