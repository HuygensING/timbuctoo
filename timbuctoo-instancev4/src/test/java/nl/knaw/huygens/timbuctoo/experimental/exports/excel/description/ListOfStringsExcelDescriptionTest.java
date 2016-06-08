package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class ListOfStringsExcelDescriptionTest {

  @Test
  public void mapsListOfStringsToCellsCorrectly() {

    final ArrayNode strings = jsnA(jsn("s1"), jsn("s2"), jsn("s3"));
    final String type = "testType";

    ExcelDescription instance = new ListOfStringsExcelDescription(strings, type);

    assertThat(instance.getCols(), equalTo(3));
    assertThat(instance.getRows(), equalTo(1));
    assertThat(instance.getType(), equalTo(type));
    assertThat(instance.getValueDescriptions(), contains("1", "2", "3"));
    assertThat(instance.getValueWidth(), equalTo(1));
    assertThat(instance.getCells(), equalTo(new String[][] {
      {"s1", "s2", "s3"}
    }));

  }
}
