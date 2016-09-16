package nl.knaw.huygens.timbuctoo.database.dto.property;

import nl.knaw.huygens.timbuctoo.database.converters.PropertyConverter;
import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.io.IOException;

public class EncodedStringOfLimitedValuesProperty extends TimProperty<String> {

  public EncodedStringOfLimitedValuesProperty(String name, String value) {
    super(name, value);
  }

  @Override
  public <TypeT> Tuple<String, TypeT> convert(PropertyConverter<TypeT> propertyConverter) throws IOException {
    return propertyConverter.to(this);
  }
}
