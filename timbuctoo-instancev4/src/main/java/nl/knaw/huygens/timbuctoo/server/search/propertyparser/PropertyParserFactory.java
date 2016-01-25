package nl.knaw.huygens.timbuctoo.server.search.propertyparser;

import nl.knaw.huygens.timbuctoo.server.search.Change;
import nl.knaw.huygens.timbuctoo.server.search.Datable;
import nl.knaw.huygens.timbuctoo.server.search.Gender;
import nl.knaw.huygens.timbuctoo.server.search.LocationNames;
import nl.knaw.huygens.timbuctoo.server.search.PersonNames;
import nl.knaw.huygens.timbuctoo.server.search.PropertyParser;

public class PropertyParserFactory {
  public PropertyParser getParser(Class<?> type) {

    if (String.class.isAssignableFrom(type)) {
      return new StringPropertyParser();
    } else if (Change.class.isAssignableFrom(type)) {
      return new ChangeDatePropertyParser();
    } else if (Datable.class.isAssignableFrom(type)) {
      return new DatableFromYearPropertyParser();
    } else if (Gender.class.isAssignableFrom(type)) {
      return new GenderPropertyParser();
    } else if (PersonNames.class.isAssignableFrom(type)) {
      return new PersonNamesDefaultNamePropertyParser();
    } else if (LocationNames.class.isAssignableFrom(type)) {
      return new DefaultLocationNamePropertyParser();
    }

    throw new UnparsableTypeException("Type " + type + " is not supported");
  }
}
