package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

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

import com.tinkerpop.blueprints.*;

import java.util.Set;

class CompositeGraphWrapper extends AbstractGraphWrapper implements GraphWrapper {
  private final Graph graph;
  private final TransactionalGraph transactionalGraph;
  private final KeyIndexableGraph keyIndexableGraph;

  public CompositeGraphWrapper(Graph graph, TransactionalGraph transactionalGraph, KeyIndexableGraph keyIndexableGraph) {
    this.graph = graph;
    this.transactionalGraph = transactionalGraph;
    this.keyIndexableGraph = keyIndexableGraph;
  }

  @Override
  protected Graph getDelegate() {
    return this.graph;
  }

  @Override
  public void stopTransaction(Conclusion conclusion) {
    this.transactionalGraph.stopTransaction(conclusion);
  }

  @Override
  public void commit() {
    this.transactionalGraph.commit();
  }

  @Override
  public void rollback() {
    this.transactionalGraph.rollback();
  }

  @Override
  public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {
    this.keyIndexableGraph.dropKeyIndex(key, elementClass);
  }

  @Override
  public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, Parameter... indexParameters) {
    this.keyIndexableGraph.createKeyIndex(key, elementClass);
  }

  @Override
  public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
    return this.keyIndexableGraph.getIndexedKeys(elementClass);
  }
}
