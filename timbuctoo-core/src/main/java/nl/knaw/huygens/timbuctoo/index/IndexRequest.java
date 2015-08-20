package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;

import java.time.LocalDateTime;

public class IndexRequest {

  public static final String INDEX_ALL = "Index all";
  private String desc;
  private Class<? extends DomainEntity> type;
  private Status status;
  private LocalDateTime lastChanged;
  private String id;

  private IndexRequest() {
    status = Status.REQUESTED;
    lastChanged = LocalDateTime.now();
  }

  public static IndexRequest forEntity(Class<? extends DomainEntity> type, String id) {
    IndexRequest indexRequest = new IndexRequest();
    indexRequest.setId(id);
    indexRequest.setType(type);
    indexRequest.setDesc(String.format("Index request for [%s] with id [%s]", TypeNames.getExternalName(type), id));
    return indexRequest;
  }

  public static IndexRequest forType(Class<? extends DomainEntity> type) {
    IndexRequest indexRequest = new IndexRequest();
    indexRequest.setType(type);
    indexRequest.setDesc(String.format("Index request for [%s]", TypeNames.getExternalName(type)));
    return indexRequest;
  }

  private void setDesc(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }

  public String toClientRep() {
    return String.format("{\"desc\":\"%s\", \"status\":\"%s\"}", desc, status);
  }

  public Class<? extends DomainEntity> getType() {
    return type;
  }

  private void setType(Class<? extends DomainEntity> type) {
    this.type = type;
  }

  public Status getStatus() {
    return status;
  }

  public void inProgress() {
    status = Status.IN_PROGRESS;
    lastChanged = LocalDateTime.now();
  }

  public void done() {
    status = Status.DONE;
    lastChanged = LocalDateTime.now();
  }

  public LocalDateTime getLastChanged() {
    return lastChanged;
  }

  private void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public StorageIterator<? extends DomainEntity> getEntities(Repository repository) {
    if(id != null){
      return StorageIteratorStub.newInstance(repository.getEntityOrDefaultVariation(type, id));
    }

    return repository.getDomainEntities(type);
  }

  public enum Status {
    REQUESTED,
    IN_PROGRESS,
    DONE
  }
}
