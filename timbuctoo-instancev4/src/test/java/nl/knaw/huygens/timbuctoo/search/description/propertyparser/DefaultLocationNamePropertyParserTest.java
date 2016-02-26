package nl.knaw.huygens.timbuctoo.search.description.propertyparser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class DefaultLocationNamePropertyParserTest extends AbstractPropertyParserTest{

  private DefaultLocationNamePropertyParser instance;

  @Before
  public void setUp() throws Exception {
    instance = new DefaultLocationNamePropertyParser();
  }

  @Test
  public void parseReturnsOutputOfPlaceNameGetDefaultName() throws JsonProcessingException {
    LocationNames locationNames = new LocationNames("defLang");
    locationNames.addCountryName("defLang", "Nederland");

    String value = instance.parse(new ObjectMapper().writeValueAsString(locationNames));

    assertThat(value, is(locationNames.getDefaultName()));
  }

  @Test
  public void parseReturnsNullIfTheInputStringCannotBeParsedToLocationNames() {
    String value = instance.parse("malformedLocationNames");

    assertThat(value, is(nullValue()));
  }

  @Test
  public void parseToRawReturnsOutputOfPlaceNameGetDefaultName() throws JsonProcessingException {
    LocationNames locationNames = new LocationNames("defLang");
    locationNames.addCountryName("defLang", "Nederland");

    Object value = instance.parseToRaw(new ObjectMapper().writeValueAsString(locationNames));

    assertThat(value, is(locationNames.getDefaultName()));
  }

  @Test
  public void parseToRawReturnsNullIfTheInputStringCannotBeParsedToLocationNames() {
    Object value = instance.parseToRaw("malformedLocationNames");

    assertThat(value, is(nullValue()));
  }

  @Override
  protected PropertyParser getInstance() {
    return instance;
  }
}
