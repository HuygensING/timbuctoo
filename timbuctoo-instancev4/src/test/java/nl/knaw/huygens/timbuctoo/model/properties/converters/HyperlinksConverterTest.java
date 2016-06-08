package nl.knaw.huygens.timbuctoo.model.properties.converters;

import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.HyperlinksExcelDescription;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class HyperlinksConverterTest {

  @Test
  public void tinkerpopToExcelReturnsAValidExcelDescriptionInstance() throws IOException {
    String dbValue = "[]";

    HyperlinksConverter instance = new HyperlinksConverter();

    ExcelDescription excelDescription = instance.tinkerPopToExcel(dbValue, "testType");

    assertThat(excelDescription, instanceOf(HyperlinksExcelDescription.class));
    assertThat(excelDescription.getType(), equalTo("testType"));
  }
}
