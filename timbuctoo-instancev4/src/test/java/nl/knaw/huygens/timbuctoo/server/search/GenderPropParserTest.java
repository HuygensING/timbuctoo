package nl.knaw.huygens.timbuctoo.server.search;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class GenderPropParserTest extends AbstractPropParserTest{

  private GenderPropParser instance;

  @Before
  public void setUp() throws Exception {
    instance = new GenderPropParser();
  }


  @Test
  public void parseReturnsUnquotedStringIfTheValueIsNotNull() {
    GenderPropParser instance = new GenderPropParser();
    String input = "\"FEMALE\"";
    String expected = "FEMALE";

    String output = instance.parse(input);

    assertThat(output, is(equalTo(expected)));
  }

  @Override
  protected PropParser getInstance() {
    return instance;
  }
}
