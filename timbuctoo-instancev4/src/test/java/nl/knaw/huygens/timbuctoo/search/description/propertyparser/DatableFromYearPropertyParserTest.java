package nl.knaw.huygens.timbuctoo.search.description.propertyparser;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class DatableFromYearPropertyParserTest extends AbstractPropertyParserTest {

  private DatableFromYearPropertyParser instance;

  @Before
  public void setUp() throws Exception {
    instance = new DatableFromYearPropertyParser();
  }

  @Test
  public void parseReturnsTheFromYearAsAStringFromAValidDatable() {
    String validDatableString = "\"2015-05-01\"";

    String result = instance.parse(validDatableString);

    assertThat(result, is("2015"));
  }

  @Test
  public void parseReturnsNullIfTheDatableIsNotValid() {
    String invalidDatableString = "hello world";

    String result = instance.parse(invalidDatableString);

    assertThat(result, is(nullValue()));
  }

  @Test
  public void parseToRawReturnsAnIntegerValueTheFromYearOfTheDatable() {
    String validDatableString = "\"2015-05-01\"";

    Object result = instance.parseToRaw(validDatableString);

    assertThat(result, is(2015));
  }

  @Test
  public void parseToRawReturnsNullIfTheDatableIsNotValid() {
    String invalidDatableString = "hello world";

    Object result = instance.parseToRaw(invalidDatableString);

    assertThat(result, is(nullValue()));
  }

  @Override
  protected PropertyParser getInstance() {
    return instance;
  }
}
