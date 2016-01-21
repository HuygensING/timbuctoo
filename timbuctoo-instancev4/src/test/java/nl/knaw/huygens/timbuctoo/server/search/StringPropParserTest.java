package nl.knaw.huygens.timbuctoo.server.search;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class StringPropParserTest extends AbstractPropParserTest{

  private StringPropParser instance;

  @Before
  public void setUp() throws Exception {
    instance = new StringPropParser();
  }

  @Test
  public void parseReturnsTheInputValue() {
    String input = "input";

    String output = instance.parse(input);

    assertThat(output, is(equalTo(input)));
  }

  @Override
  protected PropParser getInstance() {
    return instance;
  }
}
