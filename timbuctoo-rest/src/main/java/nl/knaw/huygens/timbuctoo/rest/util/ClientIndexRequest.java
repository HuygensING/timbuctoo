package nl.knaw.huygens.timbuctoo.rest.util;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class ClientIndexRequest {
  private Class<? extends DomainEntity> type;

  public ClientIndexRequest(){

  }

  public ClientIndexRequest(Class<? extends DomainEntity> type) {
    this.type = type;
  }

  public Class<? extends DomainEntity> getType() {
    return type;
  }

  public void setType(Class<? extends DomainEntity> type) {
    this.type = type;
  }
}
