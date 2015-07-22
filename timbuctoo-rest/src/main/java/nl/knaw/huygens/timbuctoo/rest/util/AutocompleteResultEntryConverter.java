package nl.knaw.huygens.timbuctoo.rest.util;

import com.google.common.collect.Maps;

import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.Entity.INDEX_FIELD_ID;
import static nl.knaw.huygens.timbuctoo.model.Entity.INDEX_FIELD_IDENTIFICATION_NAME;

public class AutocompleteResultEntryConverter {
  static final String KEY_FIELD = "key";
  static final String VALUE_FIELD = "value";

  public Map<String ,Object> convert(Map<String, Object> input) {
    Map<String, Object> result = Maps.newHashMap();

    result.put(KEY_FIELD, input.get(INDEX_FIELD_ID));
    result.put(VALUE_FIELD, input.get(INDEX_FIELD_IDENTIFICATION_NAME));

    return result;
  }
}
