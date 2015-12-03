package nl.knaw.huygens.timbuctoo.tools.conversion;

/*
 * #%L
 * Timbuctoo tools
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

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.TinkerPopStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import com.google.inject.Inject;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class TinkerPopConversionStorage extends TinkerPopStorage {

  private Graph db;
  private ElementConverterFactory converterFactory;

  @Inject
  public TinkerPopConversionStorage(Graph db, TypeRegistry typeRegistry) {
    super(db, typeRegistry);
    this.db = db;
    this.converterFactory = new ElementConverterFactory(typeRegistry);
  }

  public <T extends Entity> T getEntityByVertexId(Class<T> type, Object id) throws StorageException {
    Vertex vertex = db.getVertex(id);

    if (vertex == null) {
      throw new StorageException("Vertex with Id" + id + "not found");
    }

    return converterFactory.forType(type).convertToEntity(vertex);
  }

  public <T extends Relation> T getRelationByEdgeId(Class<T> type, Object id) throws StorageException {
    Edge edge = db.getEdge(id);

    if (edge == null) {
      throw new StorageException("Vertex with Id" + id + "not found");
    }

    return converterFactory.forRelation(type).convertToEntity(edge);
  }
}
