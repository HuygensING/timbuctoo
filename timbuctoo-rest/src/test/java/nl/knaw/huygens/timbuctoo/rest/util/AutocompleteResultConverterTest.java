package nl.knaw.huygens.timbuctoo.rest.util;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.search.RawSearchResult;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AutocompleteResultConverterTest {

  public static final java.net.URI URI = UriBuilder.fromUri("uri").build();
  private AutocompleteResultConverter instance;
  private AutocompleteResultEntryConverter entryConverter;

  @Before
  public void setup() {
    entryConverter = mock(AutocompleteResultEntryConverter.class);
    instance = new AutocompleteResultConverter(entryConverter);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void convertDelegatesToTheConversionToTheAutocompleteResultEntryConverter() {
    // setup
    Map<String, Object> input1 = Maps.<String, Object> newHashMap();
    Map<String, Object> output1 = convertsToOutput(input1);

    Map<String, Object> input2 = Maps.<String, Object> newHashMap();
    Map<String, Object> output2 = convertsToOutput(input2);

    List<Map<String, Object>> inputResults = Lists.<Map<String, Object>> newArrayList(input1, input2);
    RawSearchResult input = new RawSearchResult(2, inputResults);

    // action
    RawSearchResult convertedResult = instance.convert(input, URI);

    // verify
    assertThat(convertedResult.getResults(), containsInAnyOrder(output1, output2));
    assertThat(convertedResult.getTotal(), is(input.getTotal()));
  }

  public Map<String, Object> convertsToOutput(Map<String, Object> input) {
    Map<String, Object> output = Maps.<String, Object> newHashMap();
    when(entryConverter.convert(input, URI)).thenReturn(output);
    return output;
  }

}
