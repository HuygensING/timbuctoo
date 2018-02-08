package nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ExplicitType {

  private String name;
  @JsonProperty("fields")
  private List<ExplicitField> fields;

  @JsonCreator
  public ExplicitType(@JsonProperty("name") String name, @JsonProperty("fields") List<ExplicitField> fields) {
    this.name = name;
    this.fields = fields;
  }

  public String getName() {
    return this.name;
  }

  public List<ExplicitField> getFields() {
    return this.fields;
  }

  public Type convertToType() {
    Type convertedType = new Type(name);
    Collection<Predicate> predicates = new HashSet<>();

    if (fields == null || fields.isEmpty()) {
      return convertedType;
    }

    for (ExplicitField field : fields) {
      Predicate convertedPredicate = field.convertToPredicate();
      convertedPredicate.setIsExplicit(true);
      predicates.add(convertedPredicate);
    }
    convertedType.setPredicates(predicates);
    return convertedType;
  }

}
