package nl.knaw.huygens.timbuctoo.server;

import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.datable;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.defaultLocationNameConverter;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.gender;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.hyperlinks;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.personNames;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.stringArrayToEncodedArrayOf;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.stringOfYesNoUnknown;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.stringToEncodedStringOf;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.stringToUnencodedStringOf;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;

public class HuygensIng {

  public static Vres mappings = new Vres.Builder()
    .withVre("WomenWriters", "ww", vre -> vre
      .withCollection("wwcollectives", c -> c
        .withDisplayName(localProperty("wwcollectives_name"))

        .withProperty("name", localProperty("wwcollective_name"))
        .withProperty("type", localProperty("wwcollective_type", stringToUnencodedStringOf(
          "UNKNOWN",
          "ACADEMY",
          "ASSOCIATION",
          "LIBRARY",
          "PUBLISHER",
          "SHOP"
        )))
        .withProperty("links", localProperty("wwcollective_links", hyperlinks))

        .withProperty("tempLocationPlacename", localProperty("wwcollective_tempLocationPlacename"))
        .withProperty("tempOrigin", localProperty("wwcollective_tempOrigin"))
        .withProperty("tempShortName", localProperty("wwcollective_tempShortName"))
        .withProperty("tempType", localProperty("wwcollective_tempType"))
      )
      .withCollection("wwpersons", c -> c
        //.withDisplayName(wwpersonDisplayNameProperty())

        .withProperty("names", localProperty("wwperson_names", personNames))
        .withProperty("types", localProperty("wwperson_types", stringArrayToEncodedArrayOf(
          "ARCHETYPE",
          "AUTHOR",
          "PSEUDONYM",
          "READER"
        )))
        .withProperty("gender", localProperty("wwperson_gender", gender))
        .withProperty("birthDate", localProperty("wwperson_birthDate", datable))
        .withProperty("deathDate", localProperty("wwperson_deathDate", datable))
        .withProperty("bibliography", localProperty("wwperson_bibliography"))
        .withProperty("notes", localProperty("wwperson_notes"))
        .withProperty("children", localProperty("wwperson_children", stringOfYesNoUnknown))
        .withProperty("links", localProperty("wwperson_links", hyperlinks))

        //for passing acceptance test. I don't think these are actually used
        .withProperty("health", localProperty("wwperson_health"))
        .withProperty("livedIn", localProperty("wwperson_livedIn"))
        .withProperty("nationality", localProperty("wwperson_nationality"))
        .withProperty("personalSituation", localProperty("wwperson_personalSituation"))

        .withProperty("tempOrigin", localProperty("wwcollective_tempOrigin"))
        .withProperty("tempBirthPlace", localProperty("wwperson_tempBirthPlace"))
        .withProperty("tempChildren", localProperty("wwperson_tempChildren"))
        .withProperty("tempCollaborations", localProperty("wwperson_tempCollaborations"))
        .withProperty("tempDeath", localProperty("wwperson_tempDeath"))
        .withProperty("tempDeathPlace", localProperty("wwperson_tempDeathPlace"))
        .withProperty("tempFinancialSituation", localProperty("wwperson_tempFinancialSituation"))
        .withProperty("tempMemberships", localProperty("wwperson_tempMemberships"))
        .withProperty("tempMotherTongue", localProperty("wwperson_tempMotherTongue"))
        .withProperty("tempName", localProperty("wwperson_tempName"))
        .withProperty("tempOldId", localProperty("wwperson_tempOldId"))
        .withProperty("tempPlaceOfBirth", localProperty("wwperson_tempPlaceOfBirth"))
        .withProperty("tempPsChildren", localProperty("wwperson_tempPsChildren"))
        .withProperty("tempPseudonyms", localProperty("wwperson_tempPseudonyms"))
        .withProperty("tempSpouse", localProperty("wwperson_tempSpouse"))

        .withDerivedRelation("hasPersonLanguage", () -> {
          P<String> isWw = new P<>((types, extra) -> types.contains("\"wwrelation\""), "");
          return __
            .outE("isCreatorOf").has("isLatest", true).not(has("isDeleted", true)).has("types", isWw).inV()
            .outE("hasWorkLanguage").has("isLatest", true).not(has("isDeleted", true)).has("types", isWw).inV();
        })
      )
      .withCollection("wwdocuments", c-> c
        //.withDisplayName(wwdocumentDisplayNameProperty())
        .withProperty("title", localProperty("wwdocument_title"))
        .withProperty("englishTitle", localProperty("wwdocument_englishTitle"))
        .withProperty("documentType", localProperty("wwdocument_documentType", stringToEncodedStringOf(
          "UNKNOWN",
          "ANTHOLOGY",
          "ARTICLE",
          "AWARD",
          "CATALOGUE",
          "COMPILATION",
          "DIARY",
          "LETTER",
          "LIST",
          "MONOGRAPH",
          "PERIODICAL",
          "PICTURE",
          "PUBLICITY",
          "SHEETMUSIC",
          "THEATERSCRIPT",
          "WORK"
        )))
        .withProperty("date", localProperty("wwdocument_date", datable))
        .withProperty("reference", localProperty("wwdocument_reference"))
        .withProperty("notes", localProperty("wwdocument_notes"))
        .withProperty("links", localProperty("wwdocument_links", hyperlinks))
        .withProperty("tempCreator", localProperty("wwdocument_tempCreator"))
        .withProperty("tempLanguage", localProperty("wwdocument_tempLanguage"))
        .withProperty("tempOldId", localProperty("wwdocument_tempOldId"))
        .withProperty("tempOrigin", localProperty("wwdocument_tempOrigin"))
      )
      .withCollection("wwkeywords", c-> c
        .withDisplayName(localProperty("wwkeywords_value"))
      )
      .withCollection("wwlanguages", c-> c
        .withDisplayName(localProperty("wwlanguage_name"))
      )
      .withCollection("wwlocations", c-> c
        .withDisplayName(localProperty("wwlocation_names", defaultLocationNameConverter))
      )
      .withCollection("wwrelations")
    )
    .build();

}


