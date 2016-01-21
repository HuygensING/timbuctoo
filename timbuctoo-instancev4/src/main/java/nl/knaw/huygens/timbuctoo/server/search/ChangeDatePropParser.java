package nl.knaw.huygens.timbuctoo.server.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChangeDatePropParser implements PropParser {

  public static final Logger LOG = LoggerFactory.getLogger(ChangeDatePropParser.class);
  private final ObjectMapper objectMapper;

  public ChangeDatePropParser() {
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
