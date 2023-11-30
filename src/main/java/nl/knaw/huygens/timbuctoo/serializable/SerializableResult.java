package nl.knaw.huygens.timbuctoo.serializable;

import nl.knaw.huygens.timbuctoo.serializable.dto.QueryContainer;

public class SerializableResult {
  private final QueryContainer data;

  public SerializableResult(QueryContainer data) {
    this.data = data;
  }

  public QueryContainer getData() {
    return data;
  }
}
