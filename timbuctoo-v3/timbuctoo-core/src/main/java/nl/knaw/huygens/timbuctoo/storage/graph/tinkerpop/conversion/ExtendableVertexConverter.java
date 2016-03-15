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

import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;

import java.util.Collection;
import java.util.Set;

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
    Set<String> types = getTypesProperty(vertex);

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
