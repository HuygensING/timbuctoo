package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.graph.CorruptVertexException;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeConverter;

import java.util.Collection;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.isPrimitiveDomainEntity;
import static nl.knaw.huygens.timbuctoo.model.Entity.DB_ID_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.getTypes;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.sourceOfEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.targetOfEdge;

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
    Set<String> types = getTypes(vertex);

    for (String type : types) {
      Class<? extends DomainEntity> entity = typeRegistry.getDomainEntityType(type);
      if (isPrimitiveDomainEntity(entity)) {
        return type;
      }
    }
    throw new CorruptVertexException(vertex.getProperty(DB_ID_PROP_NAME));
  }

}
