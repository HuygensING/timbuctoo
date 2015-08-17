package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import java.time.LocalDateTime;

public class IndexRequest {

  public static final String INDEX_ALL = "Index all";
  private String desc;
  private Class<? extends DomainEntity> type;
  private Status status;
  private LocalDateTime lastChanged;

  private IndexRequest() {
    status = Status.REQUESTED;
    lastChanged = LocalDateTime.now();
  }

  public static IndexRequest indexAll() {
    IndexRequest indexRequest = new IndexRequest();
    indexRequest.setDesc(INDEX_ALL);

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


  public static IndexRequest forType(Class<? extends DomainEntity> type) {
    IndexRequest indexRequest = new IndexRequest();
    indexRequest.setType(type);
    return indexRequest;
  }

  private void setType(Class<? extends DomainEntity> type) {
    this.setDesc(String.format("Index request for [%s]", TypeNames.getExternalName(type)));
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

  public enum Status {
    REQUESTED,
    IN_PROGRESS,
    DONE
  }
}
