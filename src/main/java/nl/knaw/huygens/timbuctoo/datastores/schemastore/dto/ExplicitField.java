package nl.knaw.huygens.timbuctoo.datastores.schemastore.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExplicitField {
  @JsonProperty("uri")
  private String uri;
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

  public String getUri() {
    return uri;
  }

  public Predicate convertToPredicate() {
    Map<String, Long> valueTypes = new HashMap<>();
    Map<String, Long> referenceTypes = new HashMap<>();

    if (values != null) {
      for (String value : values) {
        valueTypes.put(value, 0L);
      }
    }

    if (references != null) {
      for (String reference : references) {
        referenceTypes.put(reference, 0L);
      }
    }

    Predicate convertedPredicate = new Predicate(uri, Direction.OUT);
    convertedPredicate.setIsExplicit(true);
    convertedPredicate.setIsList(isList);
    convertedPredicate.setValueTypes(valueTypes);
    convertedPredicate.setReferenceTypes(referenceTypes);

    return convertedPredicate;
  }

  @JsonProperty("isList")
  public boolean isList() {
    return isList;
  }

  public void setList(boolean list) {
    isList = list;
  }

  public ExplicitField mergeWith(ExplicitField explicitField) {
    ExplicitField mergedExplicitField = new ExplicitField(this.getUri(), this.isList(),
      null, null);

    if (!this.getUri().equals(explicitField.getUri())) {
      throw new IllegalArgumentException("Explicit field URIs do not match.");
    }

    mergedExplicitField.setList(this.isList() || explicitField.isList());

    Set<String> values = this.getValues();
    if (explicitField.getValues() != null) {
      values.addAll(explicitField.getValues());
    }

    mergedExplicitField.setValues(values);

    Set<String> references = this.getReferences();
    if (explicitField.getReferences() != null) {
      references.addAll(explicitField.getReferences());
    }

    mergedExplicitField.setReferences(references);

    return mergedExplicitField;
  }
}

