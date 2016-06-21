package nl.knaw.huygens.timbuctoo.model.properties.converters;

import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.StringExcelDescription;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class StringToUnencodedStringOfLimitedValuesConverterTest {


  @Test
  public void tinkerpopToExcelReturnsAValidExcelDescriptionInstance() throws IOException {
    String dbValue = "str";

    StringToUnencodedStringOfLimitedValuesConverter instance =
      new StringToUnencodedStringOfLimitedValuesConverter(new String[] {"str"});

    ExcelDescription excelDescription = instance.tinkerPopToExcel(dbValue, "string");

    assertThat(excelDescription, instanceOf(StringExcelDescription.class));
    assertThat(excelDescription.getType(), equalTo("string"));
  }
}
