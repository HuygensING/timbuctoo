package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

public class DublinCoreValueConverterTest {
  private DublinCoreValueConverter instance;

  @Before
  public void setUp() {
    instance = new DublinCoreValueConverter();
  }

  @Test
  public void convertReturnsTheToStringByDefault() {
    // setup
    Integer value = new Integer(5);

    // action
    String convertedValue = instance.convert(value);

    // verify
    assertThat(convertedValue, is(equalTo(value.toString())));
  }

  @Test
  public void convertOfAStringReturnsTheString() {
    // setup
    String value = "StringValue";

    // action
    String convertedValue = instance.convert(value);

    // verify
    assertThat(convertedValue, is(equalTo(value)));
  }

  @Test(expected = NullPointerException.class)
  public void convertThrowsANullPointerExceptionInTheValueIsNull() {
    instance.convert(null);
  }
}
