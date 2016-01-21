package nl.knaw.huygens.timbuctoo.server.search;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class DatableFromYearPropParserTest extends AbstractPropParserTest {

  private DatableFromYearPropParser instance;

  @Before
  public void setUp() throws Exception {
    instance = new DatableFromYearPropParser();
  }

  @Test
  public void parseReturnsTheFromYearAsAStringFromAValidDatable() {
    String validDatableString = "2015-05-01";

    String result = instance.parse(validDatableString);

    assertThat(result, is("2015"));
  }

  @Test
  public void parseReturnsNullIfTheDatableIsNotValid() {
    String invalidDatableString = "hello world";

    String result = instance.parse(invalidDatableString);

    assertThat(result, is(nullValue()));
  }

  @Override
  protected PropParser getInstance() {
    return instance;
  }
}
