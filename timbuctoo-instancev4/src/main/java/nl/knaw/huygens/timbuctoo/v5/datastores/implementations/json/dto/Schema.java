package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatusImpl;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.dto.Type;

import java.util.Map;

public class Schema {
  @JsonProperty
  public Map<String, Type> types;

  @JsonProperty
  public StoreStatusImpl storeStatus;
}
