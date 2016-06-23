package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.Change;

import java.io.IOException;
import java.util.Comparator;

class ChangeStringComparator implements Comparator<String> {
  private final ObjectMapper objectMapper;

  public ChangeStringComparator() {
    objectMapper = new ObjectMapper();
  }

  @Override
  public int compare(String o1, String o2) {
    try {
      long timeStamp1 = getTimestampFromChangeString(o1);
      long timeStamp2 = getTimestampFromChangeString(o2);
      return Long.compare(timeStamp1, timeStamp2);
    } catch (IOException e) {
      DatabaseFixer.LOG.error("Cannot convert change", e);
      DatabaseFixer.LOG.error("Change 1 '{}'", o1);
      DatabaseFixer.LOG.error("Change 2 '{}'", o2);
      return 0;
    }
  }

  private long getTimestampFromChangeString(String changeString) throws IOException {
    return objectMapper.readValue(changeString, Change.class).getTimeStamp();
  }
}
