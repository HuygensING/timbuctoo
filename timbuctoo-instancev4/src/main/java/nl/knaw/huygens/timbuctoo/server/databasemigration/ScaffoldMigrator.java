package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.database.DataAccess;
import nl.knaw.huygens.timbuctoo.database.DataAccessMethods;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.CollectionBuilder;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.model.vre.vres.VresBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.database.dto.RelationType.relationType;
import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.scaffoldPersonDisplayNameProperty;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.datable;

public class ScaffoldMigrator {
  private static final Logger LOG = LoggerFactory.getLogger(ScaffoldMigrator.class);
  private final DataAccess dataAccess;

  public ScaffoldMigrator(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  //FIXME move to DataAccess (allow ScaffoldVresConfig.mappings to be injected as an argument)
  public void execute() {
    //The migrations are executed first, so those vertices _will_ be present, even on a new empty database
    //The code below will add vertices, so a second launch will not run this code
    try (DataAccessMethods db = dataAccess.start()) {
      if (db.databaseIsEmptyExceptForMigrations()) {
        LOG.info("Setting up a new scaffold for empty database");
        Vres mappings = new VresBuilder()
          .withVre("Admin", "", vre ->
            vre
              .withCollection("persons", collection ->
                collection
                  .withDisplayName(scaffoldPersonDisplayNameProperty(""))
                  .withProperty("gender", localProperty("person_gender"))
                  .withProperty("birthDate", localProperty("person_birthDate", datable))
                  .withProperty("deathDate", localProperty("person_deathDate", datable))
                  .withProperty("familyName", localProperty("person_familyName"))
                  .withProperty("givenName", localProperty("person_givenName"))
                  .withProperty("preposition", localProperty("person_preposition"))
                  .withProperty("intraposition", localProperty("person_intraposition"))
                  .withProperty("postposition", localProperty("person_postposition"))
              )
              .withCollection("locations", collection ->
                collection
                  .withDisplayName(localProperty("location_name"))
                  .withProperty("name", localProperty("location_name"))
                  .withProperty("country", localProperty("location_country"))
              )
              .withCollection("collectives", collection ->
                collection
                  .withDisplayName(localProperty("collective_name"))
                  .withProperty("name", localProperty("collective_name"))
              )
              .withCollection("concepts")
              .withCollection("relations", CollectionBuilder::isRelationCollection))
          .build();
        db.initDb(
          mappings,
          relationType("person", "hasBirthPlace", "location", "isBirthPlaceOf", false, false, false, UUID.randomUUID()),
          relationType("person", "hasDeathPlace", "location", "isDeathPlaceOf", false, false, false, UUID.randomUUID()),
          relationType("collective", "hasMember", "person", "isMemberOf", false, false, false, UUID.randomUUID()),
          relationType("collective", "locatedAt", "location", "isHomeOf", false, false, false, UUID.randomUUID())
        );
        db.success();
      }
    }
  }


}
