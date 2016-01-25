package nl.knaw.huygens.timbuctoo.search.propertyparser;

import nl.knaw.huygens.timbuctoo.search.PropertyParser;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class GenderPropertyParserTest extends AbstractPropertyParserTest {

  private GenderPropertyParser instance;

  @Before
  public void setUp() throws Exception {
    instance = new GenderPropertyParser();
  }


  @Test
  public void parseReturnsUnquotedStringIfTheValueIsNotNull() {
    GenderPropertyParser instance = new GenderPropertyParser();
    String input = "\"FEMALE\"";
    String expected = "FEMALE";

    String output = instance.parse(input);

    assertThat(output, is(equalTo(expected)));
  }

  @Override
  protected PropertyParser getInstance() {
    return instance;
  }
}
