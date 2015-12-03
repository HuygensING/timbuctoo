package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

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

import java.util.Iterator;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

class TinkerPopStorageIteratorFactory {

  private final ElementConverterFactory elementConverterFactory;

  public TinkerPopStorageIteratorFactory(ElementConverterFactory elementConverterFactory) {
    this.elementConverterFactory = elementConverterFactory;
  }

  public <T extends Entity> StorageIterator<T> create(Class<T> type, Iterator<Vertex> iterator) {
    VertexConverter<T> converter = elementConverterFactory.forType(type);

    return new TinkerPopIterator<T, Vertex>(converter, iterator);
  }

  public <T extends Relation> StorageIterator<T> createForRelation(Class<T> relationType, Iterator<Edge> edges) {
    EdgeConverter<T> converter = elementConverterFactory.forRelation(relationType);

    return new TinkerPopIterator<T, Edge>(converter, edges);
  }

}
