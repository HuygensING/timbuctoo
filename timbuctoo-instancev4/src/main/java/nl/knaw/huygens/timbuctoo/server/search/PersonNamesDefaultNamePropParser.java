package nl.knaw.huygens.timbuctoo.server.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PersonNamesDefaultNamePropParser implements PropParser {

  public static final Logger LOG = LoggerFactory.getLogger(PersonNamesDefaultNamePropParser.class);

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
