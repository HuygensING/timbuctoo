package nl.knaw.huygens.timbuctoo.search.description.propertyparser;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DocumentTypePropertyParserTest extends AbstractPropertyParserTest {
  private DocumentTypePropertyParser instance;

  @BeforeEach
  public void setUp() throws Exception {
    instance = new DocumentTypePropertyParser();
  }

  @Override
  protected PropertyParser getInstance() {
    return instance;
  }

  @Test
  public void parseStripsTheExcessParenthesesFromTheValue() {
    String value = instance.parse("\"DIARY\"");

    assertThat(value, is("DIARY"));
  }

  @Test
  public void parseForSortStripsTheExcessParenthesesFromTheValue() {
    Object value = instance.parseForSort("\"DIARY\"");

    assertThat(value, is("DIARY"));
  }


}
