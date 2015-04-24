package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import java.util.Collection;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeConverter;

import com.tinkerpop.blueprints.Edge;

public class ExtendableEdgeConverter<T extends Relation> extends AbstractExtendableCovnerter<T, Edge> implements EdgeConverter<T> {

  public ExtendableEdgeConverter(Class<T> type, Collection<PropertyConverter> propertyConverters, EntityInstantiator entityInstantiator) {
    super(type, propertyConverters, entityInstantiator);
  }
}
