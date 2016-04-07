package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MigrateDatabase implements Runnable {
  public static final Logger LOG = LoggerFactory.getLogger(MigrateDatabase.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final List<DatabaseMigration> migrations;
  private final GraphWrapper graphWrapper;
  private TimbuctooConfiguration configuration;

  public MigrateDatabase(TimbuctooConfiguration configuration, GraphWrapper graphWrapper,
                         List<DatabaseMigration> migrations) {
    this.configuration = configuration;
    this.graphWrapper = graphWrapper;
    this.migrations = migrations;
  }

  @Override
  public void run() {
    try {
      List<String> executedMigrations = Lists.newArrayList();

      File executedMigrationsFile = new File(configuration.getExecutedMigrationsFilePath());
      if (executedMigrationsFile.exists()) {
        executedMigrations = MAPPER.readValue(executedMigrationsFile, new TypeReference<List<String>>() {
        });
      }

      for (DatabaseMigration migration : migrations) {
        final String name = migration.getName();
        if (!executedMigrations.contains(name)) {
          LOG.info("Executing \"{}\"", name);
          migration.execute(configuration, graphWrapper);
          executedMigrations.add(name);
          LOG.info("Finished executing \"{}\"", name);
        } else {
          LOG.info("Ignoring \"{}\" - already executed", name);
        }

      }

      MAPPER.writeValue(executedMigrationsFile, executedMigrations.toArray(new String[executedMigrations.size()]));
    } catch (IOException e) {
      LOG.error("Migration failed", e);
    }
  }
}
