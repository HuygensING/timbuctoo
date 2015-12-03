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

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoStorage;

import com.tinkerpop.blueprints.Graph;

public class ConversionVerifierFactory {

  private MongoStorage mongoStorage;
  private TinkerPopConversionStorage graphStorage;
  private Map<String, String> oldIdNewIdMap;
  private Graph graph;

  public ConversionVerifierFactory(MongoStorage mongoStorage, TinkerPopConversionStorage graphStorage, Graph graph, Map<String, String> oldIdNewIdMap) {
    this.mongoStorage = mongoStorage;
    this.graphStorage = graphStorage;
    this.graph = graph;
    this.oldIdNewIdMap = oldIdNewIdMap;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T extends DomainEntity> EntityConversionVerifier createFor(Class<T> type, int revision) {
    if (Relation.class.isAssignableFrom(type)) {
      return new RelationConversionVerifier(type, mongoStorage, graphStorage, graph, revision, oldIdNewIdMap);
    }
    return new DomainEntityConversionVerifier<T>(type, mongoStorage, graphStorage, revision);
  }
}
