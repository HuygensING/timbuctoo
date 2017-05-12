package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import nl.knaw.huygens.timbuctoo.rml.datasource.JoinHandler;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class BdbJoinHandler extends BerkeleyStore implements JoinHandler {
  private Map<String, String> fieldsToLookup = new HashMap<>();
  private static final Logger LOG = getLogger(BdbJoinHandler.class);

  public BdbJoinHandler(Environment dbEnvironment, String databaseName,
                        ObjectMapper objectMapper) throws DatabaseException {
    super(dbEnvironment, "joinHandler" + databaseName, objectMapper);
  }

  @Override
  public Map<String, List<String>> resolveReferences(Map<String, String> valueMap) {
    Map<String, List<String>> result = new HashMap<>();
    for (Map.Entry<String, String> field : fieldsToLookup.entrySet()) {
      String outputField = field.getKey();
      String sourceField = field.getValue();
      result.put(
        outputField,
        getItems(outputField + "\n" + valueMap.get(sourceField), false)
          .map(Tuple::getRight)
          .collect(toList())
      );
    }
    return result;
  }

  @Override
  public void willBeJoinedOn(String fieldName, String referenceJoinValue, String uri, String outputFieldName) {
    if (referenceJoinValue != null) {
      try {
        fieldsToLookup.put(outputFieldName, fieldName);
        put(outputFieldName + "\n" + referenceJoinValue, uri);
      } catch (DatabaseException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected DatabaseConfig getDatabaseConfig() {
    DatabaseConfig result = new DatabaseConfig();
    result.setTemporary(true);
    result.setTransactional(false);
    return result;
  }
}
