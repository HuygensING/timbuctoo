package nl.knaw.huygens.timbuctoo.server;

import nl.knaw.huygens.timbuctoo.model.properties.converters.Converters;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;

//FIXME types is een enum van specifieke waarden en verplicht
//FIXME: check of types wel echt verplicht is
public class HuygensIng {

  public static Vres mappings = new Vres.Builder()
    .withVre("WomenWriters", "ww", vre -> vre
      .withCollection("wwcollectives", c -> c
        .withProperty("type", localProperty("wwcollective_type", Converters.arrayToEncodedArray))
        .withProperty("name", localProperty("wwcollective_name"))
        .withProperty("links", localProperty("wwcollective_links"))
        .withProperty("tempType", localProperty("wwcollective_tempType"))
        .withProperty("tempLocationPlacename", localProperty("wwcollective_tempLocationPlacename"))
        .withProperty("tempOrigin", localProperty("wwcollective_tempOrigin"))
        .withProperty("tempShortName", localProperty("wwcollective_tempShortName"))
      )
      .withCollection("wwpersons", c -> c
        .withDerivedRelation("hasPersonLanguage", () -> {
          P<String> isWw = new P<>((types, extra) -> types.contains("\"wwrelation\""), "");
          return __
            .outE("isCreatorOf").has("isLatest", true).not(has("isDeleted", true)).has("types", isWw).inV()
            .outE("hasWorkLanguage").has("isLatest", true).not(has("isDeleted", true)).has("types", isWw).inV();
        })
      )
      .withCollection("wwdocuments")
      .withCollection("wwkeywords")
      .withCollection("wwlanguages")
      .withCollection("wwlocations")
      .withCollection("wwrelations")
    )
    .build();

}


