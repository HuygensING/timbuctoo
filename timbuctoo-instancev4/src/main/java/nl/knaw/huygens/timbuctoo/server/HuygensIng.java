package nl.knaw.huygens.timbuctoo.server;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.model.vre.CollectionBuilder;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.wwPersonNameOrTempName;
import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.wwdocumentDisplayNameProperty;
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

  //fixme move to database
  public static Map<String, Map<String, String>> keywordTypes = ImmutableMap.of(
    "WomenWriters",
    ImmutableMap.<String, String>builder()
      .put("hasEducation","education")
      .put("hasFinancialSituation","financialSituation")
      .put("hasGenre","genre")
      .put("hasMaritalStatus","maritalStatus")
      .put("hasProfession","profession")
      .put("hasReligion","religion")
      .put("hasSocialClass","socialClass")
      .build()
  );

  private static final String[] WW_DOCUMENT_TYPES = new String[]{
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
  };
  public static Vres mappings = new Vres.Builder()
    .withVre("WomenWriters", "ww", vre -> vre
      .withCollection("wwcollectives", c -> c
        .withDisplayName(localProperty("wwcollective_name"))

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
        .withProperty("tempOrigin", localProperty("wwcollective_tempOrigin"))
      )
      .withCollection("wwpersons", c -> c
        .withDisplayName(wwPersonNameOrTempName())

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
      .withCollection("wwdocuments", c -> c
        .withDisplayName(wwdocumentDisplayNameProperty())

        .withProperty("title", localProperty("wwdocument_title"))
        .withProperty("englishTitle", localProperty("wwdocument_englishTitle"))
        .withProperty("documentType", localProperty("wwdocument_documentType", stringToEncodedStringOf(
          WW_DOCUMENT_TYPES
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
      .withCollection("wwkeywords", c -> c
        .withDisplayName(localProperty("wwkeyword_value"))
        .withProperty("value", localProperty("wwkeyword_value"))
      )
      .withCollection("wwlanguages", c -> c
        .withDisplayName(localProperty("wwlanguage_name"))
        .withProperty("name", localProperty("wwlanguage_name"))
      )
      .withCollection("wwlocations", c -> c
        .withDisplayName(localProperty("wwlocation_names", defaultLocationNameConverter))
        .withProperty("locationName", localProperty("wwlocation_names"))
      )
      .withCollection("wwrelations", CollectionBuilder::isRelationCollection)
    )
    .withVre("cwno", "cwno", vre -> vre
      .withCollection("cwnopersons", c -> c
        .withDisplayName(localProperty("cwnoperson_name"))
        .withProperty("names", localProperty("cwnoperson_names", personNames))
        .withProperty("types", localProperty("cwnoperson_types", stringArrayToEncodedArrayOf(
          "AUTHOR",
          "PSEUDONYM"
        )))
        .withProperty("gender", localProperty("cwnoperson_gender", gender))
        .withProperty("birthDate", localProperty("cwnoperson_birthDate", datable))
        .withProperty("deathDate", localProperty("cwnoperson_deathDate", datable))
        .withProperty("notes", localProperty("cwnoperson_notes"))
        .withProperty("links", localProperty("cwnoperson_links", hyperlinks))
        .withProperty("nationalities", localProperty("cwnoperson_nationalities")))
      .withCollection("cwnodocuments", c -> c
        .withDisplayName(localProperty("cwnodocument_title"))
        .withProperty("title", localProperty("cwnodocument_title"))
        .withProperty("englishTitle", localProperty("cwnodocument_englishTitle"))
        .withProperty("documentType", localProperty("cwnodocument_documentType", stringToEncodedStringOf(
          WW_DOCUMENT_TYPES
        )))
        .withProperty("date", localProperty("cwnodocument_date", datable))
        .withProperty("notes", localProperty("cwnodocument_notes"))
        .withProperty("links", localProperty("cwnodocument_links", hyperlinks)))
      .withCollection("cwnorelations", CollectionBuilder::isRelationCollection))
    .withVre("cwrs", "cwrs", vre -> vre
      .withCollection("cwrscollectives", c -> c
        .withDisplayName(localProperty("cwrscollective_name"))

        .withProperty("name", localProperty("cwrscollective_name"))
        .withProperty("type", localProperty("cwrscollective_type", stringToUnencodedStringOf(
          "LIBRARY",
          "PUBLISHER"
        )))
        .withProperty("links", localProperty("cwrscollective_links", hyperlinks))

        .withProperty("tempLocation", localProperty("cwrscollective_tempLocation"))
        .withProperty("tempName", localProperty("cwrscollective_tempName"))
      )
      .withCollection("cwrspersons", c -> c
        .withDisplayName(localProperty("cwrsperson_name"))

        .withProperty("names", localProperty("cwrsperson_names", personNames))
        .withProperty("types", localProperty("cwrsperson_types", stringArrayToEncodedArrayOf(
          "AUTHOR",
          "PSEUDONYM"
        )))
        .withProperty("gender", localProperty("cwrsperson_gender", gender))
        .withProperty("birthDate", localProperty("cwrsperson_birthDate", datable))
        .withProperty("deathDate", localProperty("cwrsperson_deathDate", datable))
        .withProperty("links", localProperty("cwrsperson_links", hyperlinks))

        .withProperty("tempBirthPlace", localProperty("cwrsperson_tempBirthPlace"))
        .withProperty("tempDeathPlace", localProperty("cwrsperson_tempDeathPlace"))
        .withProperty("tempLanguage", localProperty("cwrsperson_tempLanguage"))
      )
      .withCollection("cwrsdocuments", c -> c
        .withDisplayName(localProperty("cwrsdocument_title"))

        .withProperty("title", localProperty("cwrsdocument_title"))
        .withProperty("documentType", localProperty("cwrsdocument_documentType", stringToEncodedStringOf(
          WW_DOCUMENT_TYPES
        )))
        .withProperty("documentResourceType",
          localProperty("cwrsdocument_documentResourceType", stringToEncodedStringOf(
            "TEXT"
          ))
        )
        .withProperty("date", localProperty("cwrsdocument_date", datable))
        .withProperty("description", localProperty("cwrsdocument_description"))
        .withProperty("links", localProperty("cwrsdocument_links", hyperlinks))
        .withProperty("tempLanguage", localProperty("cwrsdocument_tempLanguage"))))
    .withVre("dcar", "dcar", vre -> vre
    .withCollection("dcarpersons",
      c -> c
        .withDisplayName(localProperty("dcarperson_label"))
        .withProperty("gender", localProperty("dcarperson_gender", gender))
        .withProperty("names", localProperty("dcarperson_names", personNames))
        .withProperty("links", localProperty("dcarperson_links", hyperlinks)))
    .withCollection("dcarkeywords",
      c -> c
        .withDisplayName(localProperty("dcarkeyword_label"))
        .withProperty("type", localProperty("dcarkeyword_type", stringArrayToEncodedArrayOf(
          "subject",
          "geography")))
        .withProperty("value", localProperty("dcarkeyword_value"))
      )
    )
    .build();

}
