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

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.storage.DBIntegrationTestHelper;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphLegacyStorageWrapper;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQueryFactory;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class TinkerPopDBIntegrationTestHelper implements DBIntegrationTestHelper {

  private Graph graph;
  private TimbuctooQueryFactory tinkerPopQueryFactory;

  @Override
  public void startCleanDB() throws Exception {
    graph = new TinkerGraph();
    tinkerPopQueryFactory = new TimbuctooQueryFactory();
  }

  @Override
  public void stopDB() {
    graph.shutdown();
  }

  @Override
  public Storage createStorage(TypeRegistry typeRegistry) throws ModelException {
    return new GraphLegacyStorageWrapper(new TinkerPopStorage(graph, typeRegistry), tinkerPopQueryFactory);
  }

}
