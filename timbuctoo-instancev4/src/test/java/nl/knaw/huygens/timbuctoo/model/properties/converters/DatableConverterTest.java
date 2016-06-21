package nl.knaw.huygens.timbuctoo.model.properties.converters;

import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.StringExcelDescription;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class DatableConverterTest {


  @Test
  public void tinkerpopToExcelReturnsAValidExcelDescriptionInstance() throws IOException {
    DatableConverter instance = new DatableConverter();

    ExcelDescription excelDescription = instance.tinkerPopToExcel("\"1820\"", "testType");

    assertThat(excelDescription, instanceOf(StringExcelDescription.class));
    assertThat(excelDescription.getType(), equalTo("testType"));
  }
}
