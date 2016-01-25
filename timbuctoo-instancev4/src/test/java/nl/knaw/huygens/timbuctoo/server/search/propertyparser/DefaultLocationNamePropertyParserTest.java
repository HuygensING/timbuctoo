package nl.knaw.huygens.timbuctoo.server.search.propertyparser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class DefaultLocationNamePropertyParserTest {

  @Test
  public void parseReturnsOutputOfPlaceNameGetDefaultName() throws JsonProcessingException {
    LocationNames locationNames = new LocationNames("defLang");
    locationNames.addCountryName("defLang", "Nederland");
    DefaultLocationNamePropertyParser instance = new DefaultLocationNamePropertyParser();

    String value = instance.parse(new ObjectMapper().writeValueAsString(locationNames));

    assertThat(value, is(locationNames.getDefaultName()));
  }

  @Test
  public void parseReturnsNullIfTheInputStringCannotBeParsedToLocationNames() {
    DefaultLocationNamePropertyParser instance = new DefaultLocationNamePropertyParser();

    String value = instance.parse("malformedLocationNames");

    assertThat(value, is(nullValue()));
  }

  @Test
  public void parseReturnsNullIfTheInputIsNull() {
    DefaultLocationNamePropertyParser instance = new DefaultLocationNamePropertyParser();

    String value = instance.parse(null);

    assertThat(value, is(nullValue()));
  }
}
