package nl.knaw.huygens.timbuctoo.model.properties.converters;

import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ListOfStringsExcelDescription;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.StringExcelDescription;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class StringToEncodedStringOfLimitedValuesConverterTest {


  @Test
  public void tinkerpopToExcelReturnsAValidExcelDescriptionInstance() throws IOException {
    String dbValue = "\"str\"";

    StringToEncodedStringOfLimitedValuesConverter instance =
      new StringToEncodedStringOfLimitedValuesConverter("str");

    ExcelDescription excelDescription = instance.tinkerPopToExcel(dbValue, "string");

    assertThat(excelDescription, instanceOf(StringExcelDescription.class));
    assertThat(excelDescription.getType(), equalTo("string"));
  }
}
