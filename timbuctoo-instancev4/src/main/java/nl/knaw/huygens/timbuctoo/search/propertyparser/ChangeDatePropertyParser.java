package nl.knaw.huygens.timbuctoo.search.propertyparser;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.search.PropertyParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

class ChangeDatePropertyParser implements PropertyParser {

  public static final Logger LOG = LoggerFactory.getLogger(ChangeDatePropertyParser.class);
  private final ObjectMapper objectMapper;

  public ChangeDatePropertyParser() {
    objectMapper = new ObjectMapper();
  }

  @Override
  public String parse(String value) {
    if (value != null) {
      try {
        Change change = objectMapper.readValue(value, Change.class);
        Date date = new Date(change.getTimeStamp());

        return new SimpleDateFormat("yyyyMMdd").format(date);
      } catch (IOException e) {
        LOG.error("Cannot parse '{}' as Change", value);
        LOG.error("Exception thrown", e);
      }
    }

    return null;

  }
}
