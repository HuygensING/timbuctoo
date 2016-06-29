package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class HuygensIngConfigToDatabaseMigration implements DatabaseMigration {
  private static final Logger LOG = LoggerFactory.getLogger(HuygensIngConfigToDatabaseMigration.class);

  private final Vres mappings;
  private Map<String, Map<String, String>> keywordTypes;

  public HuygensIngConfigToDatabaseMigration(Vres mappings, Map<String, Map<String, String>> keywordTypes) {

    this.mappings = mappings;
    this.keywordTypes = keywordTypes;
  }

  @Override
  public void beforeMigration(GraphWrapper graphManager) {

  }

  @Override
  public void execute(GraphWrapper graphWrapper) throws IOException {
    mappings.getVres().forEach((name, vre) -> {
      vre.persistToDatabase(graphWrapper, Optional.ofNullable(keywordTypes.get(name)));
    });
  }
}
