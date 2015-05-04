package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property.PropertyConverterFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ElementConverterFactory {

  private final PropertyConverterFactory propertyConverterFactory;
  private final EntityInstantiator entityInstantiator;
  private final TypeRegistry typeRegistry;

  public ElementConverterFactory(TypeRegistry typeRegistry) {
    this(new PropertyConverterFactory(), new EntityInstantiator(), typeRegistry);
  }

  public ElementConverterFactory(PropertyConverterFactory propertyConverterFactory, EntityInstantiator entityInstantiator, TypeRegistry typeRegistry) {
    this.propertyConverterFactory = propertyConverterFactory;
    this.entityInstantiator = entityInstantiator;
    this.typeRegistry = typeRegistry;
  }

  public <T extends Entity> VertexConverter<T> forType(Class<T> type) {
    Collection<PropertyConverter> propertyConverters = createPropertyConverters(type);

    return new ExtendableVertexConverter<T>(type, propertyConverters, entityInstantiator);
  }

  // TODO make only available for DomainEntities see TIM-162
  @SuppressWarnings("unchecked")
  public <T extends Entity> VertexConverter<? super T> forPrimitiveOf(Class<T> type) {
    Class<? extends Entity> primitive = TypeRegistry.getBaseClass(type);
    VertexConverter<? extends Entity> converter = forType(primitive);

    return (VertexConverter<? super T>) converter;
  }

  public <T extends DomainEntity> VertexConverter<T> compositeForType(Class<T> type) {
    List<VertexConverter<? super T>> vertexConverters = Lists.newArrayList();
    vertexConverters.add(forType(type));
    vertexConverters.add(forPrimitiveOf(type));

    return new CompositeVertexConverter<T>(vertexConverters);
  }

  public <T extends Relation> EdgeConverter<T> forRelation(Class<T> type) {
    Collection<PropertyConverter> propertyConverters = createPropertyConverters(type);

    return new ExtendableEdgeConverter<T>(type, propertyConverters, entityInstantiator, typeRegistry);
  }

  @SuppressWarnings("unchecked")
  private <T extends Relation> EdgeConverter<? super T> forPrimitiveRelationOf(Class<T> type) {
    Class<? extends Relation> primitive = (Class<? extends Relation>) TypeRegistry.toBaseDomainEntity(type);
    EdgeConverter<? extends Relation> converter = forRelation(primitive);

    return (EdgeConverter<? super T>) converter;
  }

  public <T extends Relation> EdgeConverter<T> compositeForRelation(Class<T> relationType) {
    List<EdgeConverter<? super T>> delegates = Lists.newArrayList();
    delegates.add(forRelation(relationType));
    delegates.add(forPrimitiveRelationOf(relationType));
    return new CompositeEdgeConverter<T>(delegates);
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
