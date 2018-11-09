package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Change {
  private final String subject;
  private final String predicate;

  private final String value;
  private final String valueType;

  private final String oldValue;
  private final String oldValueType;

  public Change(String subject, String predicate, String value, String valueType, String oldValue,
                String oldValueType) {
    this.subject = subject;
    this.predicate = predicate;
    this.value = value;
    this.valueType = valueType;
    this.oldValue = oldValue;
    this.oldValueType = oldValueType;
  }

  public String getSubject() {
    return subject;
  }

  public String getPredicate() {
    return predicate;
  }

  public String getValue() {
    return value;
  }

  public String getValueType() {
    return valueType;
  }

  public String getOldValue() {
    return oldValue;
  }

  public String getOldValueType() {
    return oldValueType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Change change = (Change) o;

    return new EqualsBuilder()
      .append(subject, change.subject)
      .append(predicate, change.predicate)
      .append(value, change.value)
      .append(valueType, change.valueType)
      .append(oldValue, change.oldValue)
      .append(oldValueType, change.oldValueType)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(subject)
      .append(predicate)
      .append(value)
      .append(valueType)
      .append(oldValue)
      .append(oldValueType)
      .toHashCode();
  }

  @Override
  public String toString() {
    return "Change{" +
      "subject='" + subject + '\'' +
      ", predicate='" + predicate + '\'' +
      ", value='" + value + '\'' +
      ", valueType='" + valueType + '\'' +
      ", oldValue='" + oldValue + '\'' +
      ", oldValueType='" + oldValueType + '\'' +
      '}';
  }
}
