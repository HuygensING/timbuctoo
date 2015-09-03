package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;

import java.util.Collection;
import java.util.List;

class ExtendableVertexConverter<T extends Entity> extends AbstractExtendableElementConverter<T, Vertex> implements VertexConverter<T> {
  ExtendableVertexConverter(Class<T> type, Collection<PropertyConverter> propertyConverters, EntityInstantiator entityInstantiator) {
    super(type, propertyConverters, entityInstantiator);
  }

  @Override
  protected void executeCustomDeserializationActions(T entity, Vertex element) {
    // nothing to do
  }

  @Override
  public void removeVariant(Vertex vertex) {
    removeVariation(vertex);
    removeProperties(vertex);
  }

  private void removeVariation(Vertex vertex) {
    List<String> types = getTypesProperty(vertex);

    types.remove(TypeNames.getInternalName(type));

    setTypesProperty(vertex, types);
  }

  private void removeProperties(Vertex vertex) {
    for (PropertyConverter propertyConverter : propertyConverters()) {
      if (propertyConverter.getFieldType() != FieldType.ADMINISTRATIVE) {
        propertyConverter.removeFrom(vertex);
      }
    }
  }
}
