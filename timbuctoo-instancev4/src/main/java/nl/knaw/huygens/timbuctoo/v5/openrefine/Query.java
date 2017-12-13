package nl.knaw.huygens.timbuctoo.v5.openrefine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Query {
  public String query;
  public int limit;
  public String type;
  @JsonProperty("type_strict")
  public String typeStrict;
}
