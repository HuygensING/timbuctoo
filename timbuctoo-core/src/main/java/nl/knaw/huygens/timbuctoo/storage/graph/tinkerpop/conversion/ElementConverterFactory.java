package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property.PropertyConverterFactory;

import com.google.common.collect.Sets;

public class ElementConverterFactory {

  private final PropertyConverterFactory propertyConverterFactory;
  private final EntityInstantiator entityInstantiator;

  public ElementConverterFactory() {
    this(new PropertyConverterFactory(), new EntityInstantiator());
  }

  public ElementConverterFactory(PropertyConverterFactory propertyConverterFactory, EntityInstantiator entityInstantiator) {
    this.propertyConverterFactory = propertyConverterFactory;
    this.entityInstantiator = entityInstantiator;
  }

  public <T extends Entity> VertexConverter<T> forType(Class<T> type) {
    Collection<PropertyConverter> propertyConverters = createPropertyConverters(type);

    return new ExtendableVertexConverter<T>(type, propertyConverters, entityInstantiator);
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
