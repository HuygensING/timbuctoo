package nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExplicitField {
  @JsonProperty("uri")
  private String uri;

  public void setList(boolean list) {
    isList = list;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public List<String> getReferences() {
    return references;
  }

  public void setReferences(List<String> references) {
    this.references = references;
  }

  @JsonProperty("isList")
  private boolean isList;
  @JsonProperty("values")
  private List<String> values;
  @JsonProperty("references")
  private List<String> references;

  @JsonCreator
  public ExplicitField(@JsonProperty("uri") String uri,
                       @JsonProperty("isList") boolean isList,
                       @JsonProperty("values") List<String> values,
                       @JsonProperty("references") List<String> references) {
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


  public boolean isList() {
    return isList;
  }
}

