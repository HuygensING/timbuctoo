package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;

class ExtendableVertexConverter<T extends Entity> implements VertexConverter<T> {
  static final String VERTEX_TYPE = "^vertexType";

  private Collection<PropertyConverter> propertyConverters;

  ExtendableVertexConverter(Collection<PropertyConverter> propertyConverters) {
    this.propertyConverters = propertyConverters;

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

}