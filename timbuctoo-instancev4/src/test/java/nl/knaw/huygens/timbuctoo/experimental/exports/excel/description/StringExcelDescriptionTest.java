package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class StringExcelDescriptionTest {
  @Test
  public void mapsStringToCellsCorrectly() {

    final String value = "value";
    final String type = "testType";

    ExcelDescription instance = new StringExcelDescription(value, type);

    assertThat(instance.getCols(), equalTo(1));
    assertThat(instance.getRows(), equalTo(1));
    assertThat(instance.getType(), equalTo(type));
    assertThat(instance.getValueDescriptions(), contains(""));
    assertThat(instance.getValueWidth(), equalTo(1));
    assertThat(instance.getCells(), equalTo(new String[][] {
      {"value"}
    }));

  }
}
