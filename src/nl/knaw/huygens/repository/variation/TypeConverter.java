package nl.knaw.huygens.repository.variation;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;

/**
 * Converts between (domain) model type tokens and variation names.
 * 
 * (intermediate step: encapsulate behavior of DocTypeRegistry)
 */
class TypeConverter {

  private final DocTypeRegistry registry;

  public TypeConverter(DocTypeRegistry registry) {
    this.registry = registry;
  }

  @SuppressWarnings("unchecked")
  public <T extends Document> Class<? extends T> getClass(String id) {
    return (Class<? extends T>) registry.getClassFromMongoTypeString(id);
  }

}
