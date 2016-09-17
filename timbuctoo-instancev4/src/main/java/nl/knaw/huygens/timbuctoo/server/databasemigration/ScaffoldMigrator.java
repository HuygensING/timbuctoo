package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.database.DataAccess;
import nl.knaw.huygens.timbuctoo.database.dto.RelationType;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.databasemigration.scaffold.ScaffoldVresConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ScaffoldMigrator {
  private static final Logger LOG = LoggerFactory.getLogger(ScaffoldMigrator.class);
  private final DataAccess dataAccess;

  public ScaffoldMigrator(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  public void execute() {
    //The migrations are executed first, so those vertices _will_ be present, even on a new empty database
    //The code below will add vertices, so a second launch will not run this code
    try (DataAccess.DataAccessMethods db = dataAccess.start()) {
      if (db.databaseIsEmptyExceptForMigrations()) {
        LOG.info("Setting up a new scaffold for empty database");
        Vres mappings = ScaffoldVresConfig.mappings;
        db.initDb(
          mappings,
          new RelationType.DirectionalRelationType(
            "hasBirthPlace",
            "isBirthPlaceOf",
            "person",
            "location",
            false,
            false,
            false,
            true,
            UUID.randomUUID().toString()
          ),
          new RelationType.DirectionalRelationType(
            "hasDeathPlace",
            "isDeathPlaceOf",
            "person",
            "location",
            false,
            false,
            false,
            true,
            UUID.randomUUID().toString()
          )
        );
        db.success();
      }
    }
  }
}
