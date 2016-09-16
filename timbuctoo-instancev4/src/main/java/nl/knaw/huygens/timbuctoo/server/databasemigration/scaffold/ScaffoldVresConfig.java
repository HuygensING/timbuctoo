package nl.knaw.huygens.timbuctoo.server.databasemigration.scaffold;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.CollectionBuilder;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.model.vre.vres.VresBuilder;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.scaffoldPersonDisplayNameProperty;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.datable;

public class ScaffoldVresConfig {

  public static Vres mappings = new VresBuilder()
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
        .withCollection("relations", CollectionBuilder::isRelationCollection))
    .build(Maps.newHashMap());
}
