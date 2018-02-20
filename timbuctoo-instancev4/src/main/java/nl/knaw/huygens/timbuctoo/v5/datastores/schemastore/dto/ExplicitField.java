package nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExplicitField {
  @JsonProperty("uri")
  private String uri;

  public void setList(boolean list) {
    isList = list;
  }

  public Set<String> getValues() {
    return values;
  }

  public void setValues(Set<String> values) {
    this.values = values;
  }

  public Set<String> getReferences() {
    return references;
  }

  public void setReferences(Set<String> references) {
    this.references = references;
  }

  @JsonProperty("isList")
  private boolean isList;
  @JsonProperty("values")
  private Set<String> values;
  @JsonProperty("references")
  private Set<String> references;

  @JsonCreator
  public ExplicitField(@JsonProperty("uri") String uri,
                       @JsonProperty("isList") boolean isList,
                       @JsonProperty("values") Set<String> values,
                       @JsonProperty("references") Set<String> references) {
    this.uri = uri;
    this.isList = isList;
    this.values = values;
    this.references = references;
  }

  public String getUri() {
    return uri;
  }

  public Predicate convertToPredicate() {
    Predicate convertedPredicate = new Predicate(uri, Direction.OUT);
    Map<String, Long> valueTypes = new HashMap<>();
    Map<String, Long> referenceTypes = new HashMap<>();

    if (values != null) {
      for (String value : values) {
        valueTypes.put(value, 0L);
      }
    }

    convertedPredicate.setValueTypes(valueTypes);

    if (references != null) {
      for (String reference : references) {
        referenceTypes.put(reference, 0L);
      }
    }

    convertedPredicate.setReferenceTypes(referenceTypes);


    return convertedPredicate;
  }

  @JsonProperty("isList")
  public boolean isList() {
    return isList;
  }
}

