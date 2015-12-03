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

import java.util.Collection;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;

import com.tinkerpop.blueprints.Vertex;

public class CompositeVertexConverter<T extends Entity> implements VertexConverter<T> {

  private Collection<VertexConverter<? super T>> delegates;

  public CompositeVertexConverter(Collection<VertexConverter<? super T>> delegates) {
    this.delegates = delegates;
  }

  @Override
  public void updateElement(Vertex vertex, Entity entity) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void updateModifiedAndRev(Vertex vertexMock, Entity entity) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public String getPropertyName(String fieldName) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public <U extends T> U convertToSubType(Class<U> type, Vertex vertex) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public T convertToEntity(Vertex vertex) throws ConversionException {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void addValuesToElement(Vertex vertex, T entity) throws ConversionException {
    for (VertexConverter<? super T> vertexConverter : delegates) {
      vertexConverter.addValuesToElement(vertex, entity);
    }
  }

  int getNumberOfDelegates() {
    return delegates.size();
  }

  @Override
  public void removeVariant(Vertex vertex) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void removePropertyByFieldName(Vertex vertex, String fieldName) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
