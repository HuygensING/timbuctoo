package nl.knaw.huygens.timbuctoo.database.dto.property;

import nl.knaw.huygens.timbuctoo.database.converters.PropertyConverter;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.io.IOException;

public class DefaultFullPersonNameProperty extends TimProperty<PersonNames> {
  public DefaultFullPersonNameProperty(String name, PersonNames value) {
    super(name, value);
  }

  @Override
  public <TypeT> Tuple<String, TypeT> convert(PropertyConverter<TypeT> propertyConverter) throws IOException {
    return propertyConverter.to(this);
  }
}
