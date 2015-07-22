package nl.knaw.huygens.timbuctoo.rest.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AutocompleteResultConverterTest {

  private AutocompleteResultConverter instance;
  private AutocompleteResultEntryConverter entryConverter;

  @Before
  public void setup() {
    entryConverter = mock(AutocompleteResultEntryConverter.class);
    instance = new AutocompleteResultConverter(entryConverter);
  }

  @Test
  public void convertDelegatesToTheConversionToTheAutocompleteResultEntryConverter() {
    // setup
    Map<String, Object> input1 = Maps.<String, Object>newHashMap();
    Map<String, Object> output1 = convertsToOutput(input1);

    Map<String, Object> input2 = Maps.<String, Object>newHashMap();
    Map<String, Object> output2 = convertsToOutput(input2);

    List<Map<String, Object>> input = Lists.<Map<String, Object>>newArrayList(input1, input2);


    // action
    Iterable<Map<String, Object>> convertedResult = instance.convert(input);

    // verify
    assertThat(convertedResult, containsInAnyOrder(output1, output2));
  }

  public Map<String, Object> convertsToOutput(Map<String, Object> input) {
    Map<String, Object> output = Maps.<String, Object>newHashMap();
    when(entryConverter.convert(input)).thenReturn(output);
    return output;
  }


}
