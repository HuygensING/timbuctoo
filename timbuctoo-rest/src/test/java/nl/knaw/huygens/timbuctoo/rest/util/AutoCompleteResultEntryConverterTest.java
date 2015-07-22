package nl.knaw.huygens.timbuctoo.rest.util;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.Entity.INDEX_FIELD_ID;
import static nl.knaw.huygens.timbuctoo.model.Entity.INDEX_FIELD_IDENTIFICATION_NAME;
import static nl.knaw.huygens.timbuctoo.rest.util.AutocompleteResultEntryConverter.KEY_FIELD;
import static nl.knaw.huygens.timbuctoo.rest.util.AutocompleteResultEntryConverter.VALUE_FIELD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;

public class AutoCompleteResultEntryConverterTest {

  public static final Object ID_VALUE = "idValue";
  public static final Object DISPLAY_NAME = "displayName";
  public static final String OTHER_VALUE = "otherValue";
  public static final String OTHER_FIELD = "otherField";

  @Test
  public void convertRetrievesTheIdAndTheAndDisplayName() {
    // setup
    Map<String, Object> input = Maps.newHashMap();
    input.put(INDEX_FIELD_ID, ID_VALUE);
    input.put(INDEX_FIELD_IDENTIFICATION_NAME, DISPLAY_NAME);
    input.put(OTHER_FIELD, OTHER_VALUE);

    AutocompleteResultEntryConverter instance = new AutocompleteResultEntryConverter();

    // action
    Map<String, Object> convertedValue = instance.convert(input);

    // verify
    assertThat(convertedValue.keySet(), containsInAnyOrder(KEY_FIELD, VALUE_FIELD));

    assertThat(convertedValue, hasEntry(KEY_FIELD, ID_VALUE));
    assertThat(convertedValue, hasEntry(VALUE_FIELD, DISPLAY_NAME));
  }


}
