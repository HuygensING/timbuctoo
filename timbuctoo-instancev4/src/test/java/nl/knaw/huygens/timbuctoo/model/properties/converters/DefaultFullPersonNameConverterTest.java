package nl.knaw.huygens.timbuctoo.model.properties.converters;

import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.PersonNamesExcelDescription;
import org.junit.Test;

import java.io.IOException;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class DefaultFullPersonNameConverterTest {

  @Test
  public void tinkerpopToExcelReturnsAValidExcelDescriptionInstance() throws IOException {
    String inputJson = jsnO("list",
      jsnA(
        jsnO(
          "components", jsnA(
            jsnO(
              "type", jsn("FORENAME"),
              "value", jsn("foreName1")
            )
          )
        )
      )
    ).toString();
    DefaultFullPersonNameConverter instance = new DefaultFullPersonNameConverter();

    ExcelDescription excelDescription = instance.tinkerPopToExcel(inputJson, "testType");

    assertThat(excelDescription, instanceOf(PersonNamesExcelDescription.class));
    assertThat(excelDescription.getType(), equalTo("testType"));
  }
}
