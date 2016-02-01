package nl.knaw.huygens.timbuctoo.model.vre.neww;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.JsonToTinkerpopPropertyMap;
import nl.knaw.huygens.timbuctoo.model.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.converters.Converters;

import java.util.List;
import java.util.Map;

public class JsonToTinkerpopMappings {
  private static Map<String, List<JsonToTinkerpopPropertyMap>> mappings = ImmutableMap.of(
    "wwcollectives", Lists.newArrayList(
      new JsonToTinkerpopPropertyMap("type", "wwcollective_type"),
      new JsonToTinkerpopPropertyMap("name", "wwcollective_name"),
      //"acronym": null,
      //"period": null,
      new JsonToTinkerpopPropertyMap("links", "wwcollective_links", Converters.arrayToEncodedArray),
      new JsonToTinkerpopPropertyMap("notes", "wwcollective_notes"),
      new JsonToTinkerpopPropertyMap("tempType", "wwcollective_tempType"),
      new JsonToTinkerpopPropertyMap("tempLocationPlacename", "wwcollective_tempLocationPlacename"),
      new JsonToTinkerpopPropertyMap("tempOrigin", "wwcollective_tempOrigin"),
      new JsonToTinkerpopPropertyMap("tempShortName", "wwcollective_tempShortName")
    )
  );

  public static Map<String, List<JsonToTinkerpopPropertyMap>> getMappings() {
    return mappings;
  }
}


