package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class AltNamesExcelDescriptionTest {

  @Test
  public void mapsAltNamesToCellsCorrectly() {

    final ArrayNode altNames = jsnA(
      jsnO("nametype", jsn("test"), "displayName", jsn("Name 1")),
      jsnO("nametype", jsn("test2"), "displayName", jsn("Name 2"))
    );
    final String type = "testType";

    ExcelDescription instance = new AltNamesExcelDescription(altNames, type);

    assertThat(instance.getCols(), equalTo(4));
    assertThat(instance.getRows(), equalTo(2));
    assertThat(instance.getType(), equalTo(type));
    assertThat(instance.getValueDescriptions(), contains("1", "2"));
    assertThat(instance.getValueWidth(), equalTo(2));
    assertThat(instance.getCells(), equalTo(new String[][] {
      {"nametype", "test", "nametype", "test2"},
      {"displayName", "Name 1", "displayName", "Name 2"}
    }));

  }
}
