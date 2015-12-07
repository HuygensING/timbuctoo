package nl.knaw.huygens.timbuctoo.config;

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

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.impls.rexster.RexsterGraph;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphLegacyStorageWrapper;
import nl.knaw.huygens.timbuctoo.storage.graph.GraphStorage;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.TinkerPopStorage;

public class BasicInjectionModule extends AbstractModule {

  protected final Configuration config;
  private final TypeRegistry registry;

  public BasicInjectionModule(Configuration config) {
    try {
      this.config = config;
      registry = TypeRegistry.getInstance();
      registry.init(config.getSetting("model-packages"));
      validateConfig(config);
    } catch (Exception e) {
      // TODO throw checked exception
      throw new RuntimeException(e);
    }
  }

  protected void validateConfig(Configuration config) {
    new ConfigValidator(config).validate();
  }

  @Override
  protected void configure() {
    Names.bindProperties(binder(), config.getAll());
    bind(Configuration.class).toInstance(config);
    bind(TypeRegistry.class).toInstance(registry);

    bind(Storage.class).to(GraphLegacyStorageWrapper.class);
    bind(GraphStorage.class).to(TinkerPopStorage.class);
    bindGraph();

  }

  protected void bindGraph() {
    bind(Graph.class).toInstance(createDB());
  }

  private Graph createDB() {
    GraphTypes type = GraphTypes.valueOf(config.getSetting("graph.type"));
    if (type == GraphTypes.NEO4J) {
      return new Neo4j2Graph(config.getDirectory("graph.path"));
    } else if (type == GraphTypes.REXSTER) {
      return createRexsterGraph();
    }
    throw new RuntimeException("Database" + type + " is not supported yet.");
  }

  public Graph createRexsterGraph() {
    String user = config.getSetting("graph.user", null);
    String password = config.getSetting("graph.password", null);
    String url = config.getSetting("graph.url");

    return new RexsterGraph(url, user, password);
  }
}
