package nl.knaw.huygens.timbuctoo.model.properties.converters;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class StringToEncodedStringOfLimitedValuesConverterTest {

  @Test
  public void validateThrowsAnExceptionWhenTheValueIsNotAnExpectedValue() throws IOException {
    Assertions.assertThrows(IOException.class, () -> {
      StringToEncodedStringOfLimitedValuesConverter instance =
          new StringToEncodedStringOfLimitedValuesConverter("val1", "val2");

      instance.validate("\"val3\"");
    });
  }

  @Test
  public void validateDoesNothingWhenTheValueIsAnExpectedValue() throws IOException {
    StringToEncodedStringOfLimitedValuesConverter instance =
      new StringToEncodedStringOfLimitedValuesConverter("val1", "val2");

    instance.validate("\"val2\"");
  }
}
