package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.isPrimitiveDomainEntity;
import static nl.knaw.huygens.timbuctoo.model.Entity.DB_ID_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.getTypes;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.sourceOfEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.targetOfEdge;

import java.util.Collection;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.CorruptVertexException;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeConverter;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class ExtendableEdgeConverter<T extends Relation> extends AbstractExtendableElementConverter<T, Edge> implements EdgeConverter<T> {

  private final TypeRegistry typeRegistry;

  public ExtendableEdgeConverter(Class<T> type, Collection<PropertyConverter> propertyConverters, EntityInstantiator entityInstantiator, TypeRegistry typeRegistry) {
    super(type, propertyConverters, entityInstantiator);
    this.typeRegistry = typeRegistry;
  }

  @Override
  protected void executeCustomDeserializationActions(T entity, Edge element) {

    Vertex source = sourceOfEdge(element);
    entity.setSourceId(source.<String> getProperty(DB_ID_PROP_NAME));
    entity.setSourceType(getPrimitiveType(source));

    Vertex target = targetOfEdge(element);
    entity.setTargetId(target.<String> getProperty(DB_ID_PROP_NAME));
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
    throw new CorruptVertexException(vertex.getProperty(DB_ID_PROP_NAME));
  }

}
