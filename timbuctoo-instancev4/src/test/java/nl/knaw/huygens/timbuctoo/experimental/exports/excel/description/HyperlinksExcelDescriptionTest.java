package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class HyperlinksExcelDescriptionTest {


  @Test
  public void mapsHyperlinksToCellsCorrectly() {

    final ArrayNode hyperLinks = jsnA(
      jsnO("label", jsn("test"), "url", jsn("url.1")),
      jsnO("label", jsn("test2"), "url", jsn("url.2"))
    );
    final String type = "testType";

    ExcelDescription instance = new HyperlinksExcelDescription(hyperLinks, type);

    assertThat(instance.getCols(), equalTo(4));
    assertThat(instance.getRows(), equalTo(2));
    assertThat(instance.getType(), equalTo(type));
    assertThat(instance.getValueDescriptions(), contains("1", "2"));
    assertThat(instance.getValueWidth(), equalTo(2));
    assertThat(instance.getCells(), equalTo(new String[][] {
      {"label", "test", "label", "test2"},
      {"url", "url.1", "url", "url.2"}
    }));

  }
}
