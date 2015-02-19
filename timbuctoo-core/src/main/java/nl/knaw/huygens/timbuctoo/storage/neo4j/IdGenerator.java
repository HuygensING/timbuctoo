package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.UUID;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.model.Entity;

public class IdGenerator {

  // TODO find a solution for support legacy id's 
  public String nextIdFor(Class<? extends Entity> type) {
    return String.format("%s%s", getIdPrefix(type), UUID.randomUUID());
  }

  private String getIdPrefix(Class<? extends Entity> type) {
    IDPrefix idPrefix = type.getAnnotation(IDPrefix.class);

    return idPrefix != null ? idPrefix.value() : "UNKN";
  }

}
