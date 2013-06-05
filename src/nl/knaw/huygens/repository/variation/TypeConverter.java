package nl.knaw.huygens.repository.variation;

import java.util.Map;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;

import com.google.common.collect.Maps;

/**
 * Converts between (domain) model type tokens and variation names.
 * The conversion rules are used by VariationReducer and VariationInducer only.
 */
class TypeConverter {

  private final Map<String, Class<? extends Document>> map;

  public TypeConverter(DocTypeRegistry registry) {
    map = Maps.newHashMap();
    for (Class<? extends Document> type : registry.getDocumentTypes()) {
      map.put(type.getSimpleName().toLowerCase(), type);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Document> Class<? extends T> getClass(String id) {
    return (Class<? extends T>) map.get(normalize(id));
  }

  private String normalize(String typeString) {
    return typeString.replaceFirst("[a-z]*-", "");
  }

}
