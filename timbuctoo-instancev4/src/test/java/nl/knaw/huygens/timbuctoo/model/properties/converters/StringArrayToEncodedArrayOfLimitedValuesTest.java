package nl.knaw.huygens.timbuctoo.model.properties.converters;

import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ListOfStringsExcelDescription;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class StringArrayToEncodedArrayOfLimitedValuesTest {

  @Test
  public void tinkerpopToExcelReturnsAValidExcelDescriptionInstance() throws IOException {
    String dbValue = "[]";

    StringArrayToEncodedArrayOfLimitedValues instance = new StringArrayToEncodedArrayOfLimitedValues();

    ExcelDescription excelDescription = instance.tinkerPopToExcel(dbValue, "list-of-strings");

    assertThat(excelDescription, instanceOf(ListOfStringsExcelDescription.class));
    assertThat(excelDescription.getType(), equalTo("list-of-strings"));
  }
}
