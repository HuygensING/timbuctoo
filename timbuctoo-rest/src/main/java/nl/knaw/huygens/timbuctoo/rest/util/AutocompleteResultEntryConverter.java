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

import java.net.URI;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.Entity.INDEX_FIELD_ID;
import static nl.knaw.huygens.timbuctoo.model.Entity.INDEX_FIELD_IDENTIFICATION_NAME;

public class AutocompleteResultEntryConverter {
  static final String KEY_FIELD = "key";
  static final String VALUE_FIELD = "value";

  public Map<String ,Object> convert(Map<String, Object> input, URI uri) {
    Map<String, Object> result = Maps.newHashMap();

    result.put(KEY_FIELD, input.get(INDEX_FIELD_ID));
    result.put(VALUE_FIELD, input.get(INDEX_FIELD_IDENTIFICATION_NAME));

    return result;
  }

  public String getLink(Map<String, Object> input, URI uri) {
    return String.format("%s/%s",uri, input.get(INDEX_FIELD_ID));
  }
}
