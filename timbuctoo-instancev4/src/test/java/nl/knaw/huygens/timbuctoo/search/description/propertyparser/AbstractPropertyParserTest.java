package nl.knaw.huygens.timbuctoo.search.description.propertyparser;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public abstract class AbstractPropertyParserTest {
  @Test
  public void parseReturnsNullIfTheInputIsNull() {

    String output = getInstance().parse(null);

    assertThat(output, is(nullValue()));
  }

  protected abstract PropertyParser getInstance();
}
