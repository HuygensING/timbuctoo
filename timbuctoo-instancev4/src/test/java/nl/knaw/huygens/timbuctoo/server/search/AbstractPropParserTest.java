package nl.knaw.huygens.timbuctoo.server.search;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public abstract class AbstractPropParserTest {
  @Test
  public void parseReturnsNullIfTheInputIsNull() {

    String output = getInstance().parse(null);

    assertThat(output, is(nullValue()));
  }

  protected abstract PropParser getInstance();
}
