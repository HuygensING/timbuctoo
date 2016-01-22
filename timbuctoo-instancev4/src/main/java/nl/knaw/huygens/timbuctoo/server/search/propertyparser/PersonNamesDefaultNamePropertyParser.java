package nl.knaw.huygens.timbuctoo.server.search.propertyparser;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.server.search.PersonNames;
import nl.knaw.huygens.timbuctoo.server.search.PropertyParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class PersonNamesDefaultNamePropertyParser implements PropertyParser {

  private static final Logger LOG = LoggerFactory.getLogger(PersonNamesDefaultNamePropertyParser.class);

  @Override
  public String parse(String value) {
    if (value != null) {
      try {
        PersonNames personNames = new ObjectMapper().readValue(value, PersonNames.class);

        return personNames.defaultName().getShortName();
      } catch (IOException e) {
        LOG.error("Cannot parse '{}' as Change", value);
        LOG.error("Exception thrown", e);
      }
    }

    return null;
  }
}
