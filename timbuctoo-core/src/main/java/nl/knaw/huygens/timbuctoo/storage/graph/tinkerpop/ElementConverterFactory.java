package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.collect.Sets;

public class ElementConverterFactory {

  private final PropertyConverterFactory propertyConverterFactory;

  public ElementConverterFactory() {
    this(new PropertyConverterFactory());
  }

  public ElementConverterFactory(PropertyConverterFactory propertyConverterFactory) {
    this.propertyConverterFactory = propertyConverterFactory;
  }

  public <T extends Entity> VertexConverter<T> forType(Class<T> type) {
    Collection<PropertyConverter> propertyConverters = createPropertyConverters(type);

    return new ExtendableVertexConverter<T>(propertyConverters);
  }

  @SuppressWarnings("unchecked")
  private <T extends Entity> Collection<PropertyConverter> createPropertyConverters(Class<T> type) {
    Set<PropertyConverter> propertyConverters = Sets.newConcurrentHashSet();
    for (Class<? extends Entity> typeToGetFieldsFrom = type; isEntity(typeToGetFieldsFrom); typeToGetFieldsFrom = (Class<? extends Entity>) typeToGetFieldsFrom.getSuperclass()) {

      for (Field field : typeToGetFieldsFrom.getDeclaredFields()) {
        propertyConverters.add(propertyConverterFactory.createPropertyConverter(type, field));
      }
    }
    return propertyConverters;
  }

  private boolean isEntity(Class<? extends Entity> typeToGetFieldsFrom) {
    return Entity.class.isAssignableFrom(typeToGetFieldsFrom);
  }

}
