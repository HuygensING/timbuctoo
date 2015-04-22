package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexFields.VERTEX_TYPE;

import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;

class ExtendableVertexConverter<T extends Entity> implements VertexConverter<T> {

  private final Collection<PropertyConverter> propertyConverters;
  private final EntityInstantiator entityInstantiator;
  private final Class<T> type;

  ExtendableVertexConverter(Class<T> type, Collection<PropertyConverter> propertyConverters, EntityInstantiator entityInstantiator) {
    this.type = type;
    this.propertyConverters = propertyConverters;
    this.entityInstantiator = entityInstantiator;

  }

  @Override
  public void addValuesToVertex(Vertex vertex, T entity) throws ConversionException {
    addVariation(vertex, entity.getClass());
    for (PropertyConverter propertyConverter : propertyConverters) {
      propertyConverter.setValueOfVertex(vertex, entity);
    }
  }

  private void addVariation(Vertex vertex, Class<? extends Entity> variationType) {
    String[] types = (String[]) (vertex.getProperty(VERTEX_TYPE) != null ? vertex.getProperty(VERTEX_TYPE) : new String[] {});
    List<String> typeList = Lists.newArrayList(types);
    typeList.add(TypeNames.getInternalName(variationType));

    vertex.setProperty(VERTEX_TYPE, typeList.toArray(new String[typeList.size()]));
  }

  @Override
  public T convertToEntity(Vertex vertex) throws ConversionException {
    try {
      T entity = entityInstantiator.createInstanceOf(type);

      for (PropertyConverter propertyConverter : propertyConverters) {
        propertyConverter.addValueToEntity(entity, vertex);
      }

      return entity;

    } catch (InstantiationException e) {
      throw new ConversionException("Entity could not be instantiated.");
    }

  }
}