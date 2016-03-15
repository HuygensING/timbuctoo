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

import com.google.common.collect.Maps;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
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
  public static final URI ENTITY_URI = UriBuilder.fromUri("uri").build();
  public static final Object KEY_VALUE = String.format("%s/%s", ENTITY_URI, ID_VALUE);

  @Test
  public void convertRetrievesTheIdAndTheAndDisplayName() {
    // setup
    Map<String, Object> input = Maps.newHashMap();
    input.put(INDEX_FIELD_ID, ID_VALUE);
    input.put(INDEX_FIELD_IDENTIFICATION_NAME, DISPLAY_NAME);
    input.put(OTHER_FIELD, OTHER_VALUE);

    AutocompleteResultEntryConverter instance = new AutocompleteResultEntryConverter();

    // action
    Map<String, Object> convertedValue = instance.convert(input, ENTITY_URI);

    // verify
    assertThat(convertedValue.keySet(), containsInAnyOrder(KEY_FIELD, VALUE_FIELD));

    assertThat(convertedValue, hasEntry(KEY_FIELD, KEY_VALUE));
    assertThat(convertedValue, hasEntry(VALUE_FIELD, DISPLAY_NAME));
  }


}
