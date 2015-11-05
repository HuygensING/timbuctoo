package nl.knaw.huygens.timbuctoo.tools.conversion;

public class Mismatch {

  private final String fieldName;
  private final Object oldValue;
  private final Object newValue;

  public Mismatch(String fieldName, Object oldValue, Object newValue) {
    this.fieldName = fieldName;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  @Override
  public String toString() {
    return String.format("Field \"%s\" does not match. Old value is \"%s\" and new value is \"%s\"", fieldName, oldValue, newValue);
  }
}
