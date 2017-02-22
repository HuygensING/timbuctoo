package nl.knaw.huygens.timbuctoo.crud.conversion.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.core.dto.property.AltNamesProperty;
import nl.knaw.huygens.timbuctoo.crud.conversion.JsonPropertyConverter;
import nl.knaw.huygens.timbuctoo.model.AltName;
import nl.knaw.huygens.timbuctoo.model.AltNames;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;


public class JsonPropertyConverterTest {
  @Test
  public void toConvertsTheListOfAltNamesToAnArrayNode() throws Exception {
    JsonPropertyConverter jsonPropertyConverter = new JsonPropertyConverter(null);
    AltNames altNames = new AltNames();
    altNames.list = Lists.newArrayList(new AltName("type1", "name1"), new AltName("type2", "name2"));

    Tuple<String, JsonNode> valueTuple = jsonPropertyConverter.to(new AltNamesProperty("test", altNames));

    JsonNode value = valueTuple.getRight();

    assertThat(value.isArray(), is(true));
    assertThat(Lists.newArrayList(value.iterator()), containsInAnyOrder(
      jsnO("nametype", jsn("type1"), "displayName", jsn("name1")),
      jsnO("nametype", jsn("type2"), "displayName", jsn("name2"))
    ));
  }
}
