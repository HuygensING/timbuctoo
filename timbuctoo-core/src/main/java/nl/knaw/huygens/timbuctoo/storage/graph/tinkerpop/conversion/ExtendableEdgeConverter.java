package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.isPrimitiveDomainEntity;
import static nl.knaw.huygens.timbuctoo.model.Entity.ID_DB_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.sourceOfEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.targetOfEdge;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.CorruptNodeException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeConverter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class ExtendableEdgeConverter<T extends Relation> extends AbstractExtendableConverter<T, Edge> implements EdgeConverter<T> {

  private final TypeRegistry typeRegistry;

  public ExtendableEdgeConverter(Class<T> type, Collection<PropertyConverter> propertyConverters, EntityInstantiator entityInstantiator, TypeRegistry typeRegistry) {
    super(type, propertyConverters, entityInstantiator);
    this.typeRegistry = typeRegistry;
  }

  @Override
  protected void executeCustomDeserializationActions(T entity, Edge element) {

    Vertex source = sourceOfEdge(element);
    entity.setSourceId(source.<String> getProperty(ID_DB_PROPERTY_NAME));
    entity.setSourceType(getPrimitiveType(source));

    Vertex target = targetOfEdge(element);
    entity.setTargetId(target.<String> getProperty(ID_DB_PROPERTY_NAME));
    entity.setTargetType(getPrimitiveType(target));
  }

  private String getPrimitiveType(Vertex vertex) {
    List<String> types = getTypes(vertex);

    for (String type : types) {
      Class<? extends DomainEntity> entity = typeRegistry.getDomainEntityType(type);
      if (isPrimitiveDomainEntity(entity)) {
        return type;
      }
    }
    throw new CorruptNodeException(vertex.getProperty(ID_DB_PROPERTY_NAME));
  }

  protected List<String> getTypes(Vertex vertex) {
    ObjectMapper objectMapper = new ObjectMapper();

    List<String> types = null;
    try {
      types = objectMapper.readValue((String) vertex.getProperty(ELEMENT_TYPES), new TypeReference<List<String>>() {});
    } catch (IOException e) {
      e.printStackTrace();
    }

    return types != null ? types : Lists.<String> newArrayList();
  }
}
