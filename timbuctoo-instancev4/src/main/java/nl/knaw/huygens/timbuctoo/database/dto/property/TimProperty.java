package nl.knaw.huygens.timbuctoo.database.dto.property;

import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.io.IOException;

public abstract class TimProperty<ValueT> {
  private final String name;
  private final ValueT value;

  public TimProperty(String name, ValueT value) {
    this.name = name;
    this.value = value;
  }

  public abstract <TypeT> Tuple<String, TypeT> convert(PropertyConverter<TypeT> propertyConverter) throws IOException;

  public String getName() {
    return name;
  }

  public ValueT getValue() {
    return value;
  }
}
