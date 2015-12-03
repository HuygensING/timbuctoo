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

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.ElementConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SystemEntityCollectionConverter<T extends SystemEntity> {
  private static final Logger LOG = LoggerFactory.getLogger(SystemEntityCollectionConverter.class);
  private final Class<T> type;
  private final MongoConversionStorage mongoStorage;
  private final Graph graph;
  private final TinkerPopConversionStorage graphStorage;
  private final ElementConverterFactory converterFactory;
  private final IdGenerator idGenerator;
  private final Map<String, String> oldIdNewIdMap;

  public SystemEntityCollectionConverter(Class<T> type, MongoConversionStorage mongoStorage, Graph graph, TinkerPopConversionStorage graphStorage, ElementConverterFactory converterFactory,
      IdGenerator idGenerator, Map<String, String> oldIdNewIdMap) {
    this.type = type;
    this.mongoStorage = mongoStorage;
    this.graph = graph;
    this.graphStorage = graphStorage;
    this.converterFactory = converterFactory;
    this.idGenerator = idGenerator;
    this.oldIdNewIdMap = oldIdNewIdMap;
  }

  public void convert() throws StorageException, ConversionException, IllegalAccessException {
    LOG.info("Converting {}", type.getSimpleName());
    for (StorageIterator<T> iterator = mongoStorage.getSystemEntities(type); iterator.hasNext();) {
      T entity = iterator.next();

      convertSystemEntity(type, entity);
    }
  }

  private Vertex convertSystemEntity(Class<T> type, T entity) throws ConversionException, StorageException, IllegalAccessException {
    SystemEntityConversionVerifier<T> conversionChecker = new SystemEntityConversionVerifier<T>(type, mongoStorage, graphStorage);
    String oldId = entity.getId();
    String newId = mapOldIdtoNewId(type, entity);
    entity.setId(newId);

    Vertex vertex = graph.addVertex(null);
    vertex.setProperty(ElementFields.IS_LATEST, true);
    addPropertiesToVertex(type, entity, vertex);

    conversionChecker.verifyConversion(oldId, newId, vertex.getId());

    return vertex;
  }

  private <U extends Entity> void addPropertiesToVertex(Class<T> type, T entity, Vertex vertex) throws ConversionException {
    VertexConverter<T> converter = converterFactory.forType(type);

    converter.addValuesToElement(vertex, entity);
  }

  private <U extends Entity> String mapOldIdtoNewId(Class<T> type, T entity) {
    String oldId = entity.getId();

    String newId = idGenerator.nextIdFor(type);
    oldIdNewIdMap.put(oldId, newId);
    return newId;
  }
}
