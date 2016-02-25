package nl.knaw.huygens.timbuctoo.search.description.propertyparser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class PersonNamesDefaultNamePropertyParser implements PropertyParser {

  private static final Logger LOG = LoggerFactory.getLogger(PersonNamesDefaultNamePropertyParser.class);

  @Override
  public String parse(String value) {
    if (value != null) {
      try {
        PersonNames personNames = readPersonNames(value);

        return personNames.defaultName().getShortName();
      } catch (IOException e) {
        LOG.error("Cannot parse '{}' as Change", value);
        LOG.error("Exception thrown", e);
      }
    }

    return null;
  }

  protected PersonNames readPersonNames(String value) throws IOException {
    return new ObjectMapper().readValue(value, PersonNames.class);
  }

  @Override
  public Object parseToRaw(String value) {
    if (value != null) {
      try {
        PersonNames personNames = readPersonNames(value);

        return Joiner.on(" ").join(personNames.list);
      } catch (IOException e) {
        LOG.error("Cannot parse '{}' as Change", value);
        LOG.error("Exception thrown", e);
      }
    }

    return getDefaultValue();
  }

  @Override
  public Object getDefaultValue() {
    return "";
  }
}
