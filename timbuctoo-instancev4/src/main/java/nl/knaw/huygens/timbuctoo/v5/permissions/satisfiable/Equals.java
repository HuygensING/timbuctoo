package nl.knaw.huygens.timbuctoo.v5.permissions.satisfiable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class Equals implements Satisfiable {

  private final String expected;

  @JsonCreator
  public Equals(@JsonProperty("expected") String expected) {
    if (expected == null) {
      throw new IllegalStateException("Expected value must be non-null");
    }
    this.expected = expected;
  }

  @Override
  public Boolean apply(String actual) {
    return expected.equals(actual);
  }
}
