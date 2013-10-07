package nl.knaw.huygens.timbuctoo.variation;

import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;

/**
 * Converts between (domain) model type tokens and variation names.
 * The conversion rules are used by VariationReducer and VariationInducer only.
 */
class TypeConverter {

  private final DocTypeRegistry registry;

  public TypeConverter(DocTypeRegistry registry) {
    this.registry = registry;
  }

  @SuppressWarnings("unchecked")
  public <T extends Entity> Class<? extends T> getClass(String id) {
    return (Class<? extends T>) registry.getTypeForIName(normalize(id));
  }

  private String normalize(String typeString) {
    return typeString.replaceFirst("[a-z]*-", "");
  }

}
