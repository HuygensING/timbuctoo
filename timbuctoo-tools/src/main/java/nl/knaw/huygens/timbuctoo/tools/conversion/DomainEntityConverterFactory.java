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

import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexDuplicator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;

import com.tinkerpop.blueprints.Graph;

public class DomainEntityConverterFactory {

  private MongoConversionStorage mongoStorage;
  private IdGenerator idGenerator;
  private RevisionConverter revisionConverter;
  private VertexDuplicator vertexDuplicator;
  private Map<String, String> oldIdNewIdMap;
  private Map<String, Object> oldIdLatestVertexIdMap;

  public DomainEntityConverterFactory(MongoConversionStorage mongoStorage, Graph graph, TypeRegistry typeRegistry, TinkerPopConversionStorage graphStorage, Map<String, String> oldIdNewIdMap,
      Map<String, Object> oldIdLatestVertexIdMap) {
    this.mongoStorage = mongoStorage;
    this.oldIdNewIdMap = oldIdNewIdMap;
    this.oldIdLatestVertexIdMap = oldIdLatestVertexIdMap;
    this.idGenerator = new IdGenerator();
    this.revisionConverter = new RevisionConverter(graph, new VariationConverter(new ElementConverterFactory(typeRegistry)), new ConversionVerifierFactory(mongoStorage, graphStorage, graph,
        oldIdNewIdMap));
    this.vertexDuplicator = new VertexDuplicator(graph, null);
  }

  public <T extends DomainEntity> DomainEntityConverter<T> create(Class<T> type, String id) {
    return new DomainEntityConverter<T>(type, id, mongoStorage, idGenerator, revisionConverter, vertexDuplicator, oldIdNewIdMap, oldIdLatestVertexIdMap);
  }

}
