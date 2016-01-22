package nl.knaw.huygens.timbuctoo.server.search.propertyparser;

import nl.knaw.huygens.timbuctoo.server.search.Change;
import nl.knaw.huygens.timbuctoo.server.search.Datable;
import nl.knaw.huygens.timbuctoo.server.search.Gender;
import nl.knaw.huygens.timbuctoo.server.search.PersonNames;
import nl.knaw.huygens.timbuctoo.server.search.PropertyParser;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class PropertyParserFactoryTest {

  private PropertyParserFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new PropertyParserFactory();
  }

  @Test
  public void getParserReturnsAStringPropertyParserForString() {
    PropertyParser parser = instance.getParser(String.class);

    assertThat(parser, is(instanceOf(StringPropertyParser.class)));
  }

  private static class UnknownType {

  }

  @Test(expected = UnparsableTypeException.class)
  public void getParserThrowsAUnparsableTypeExceptionWhenNoParserIsKnowForTheType() {
    instance.getParser(UnknownType.class);
  }


  @Test
  public void getParserReturnsAChangeDatePropertyParserForChange() {
    PropertyParser parser = instance.getParser(Change.class);

    assertThat(parser, is(instanceOf(ChangeDatePropertyParser.class)));
  }

  @Test
  public void getParserReturnsADatableFromYearPropertyParserForDatable() {
    PropertyParser parser = instance.getParser(Datable.class);

    assertThat(parser, is(instanceOf(DatableFromYearPropertyParser.class)));
  }

  @Test
  public void getParserReturnsAGenderPropertyParserForGender() {
    PropertyParser parser = instance.getParser(Gender.class);

    assertThat(parser, is(instanceOf(GenderPropertyParser.class)));
  }

  @Test
  public void getParserReturnsAPersonNamesDefaultPropertyParserFor() {
    PropertyParser parser = instance.getParser(PersonNames.class);

    assertThat(parser, is(instanceOf(PersonNamesDefaultNamePropertyParser.class)));
  }
}
