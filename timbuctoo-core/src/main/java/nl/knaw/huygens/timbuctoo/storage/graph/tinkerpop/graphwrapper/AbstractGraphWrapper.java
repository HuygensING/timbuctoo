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

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;

abstract class AbstractGraphWrapper implements Graph {

  protected abstract Graph getDelegate();

  @Override
  public Features getFeatures() {
    return getDelegate().getFeatures();
  }

  @Override
  public Vertex addVertex(Object id) {
    return getDelegate().addVertex(id);
  }

  @Override
  public Vertex getVertex(Object id) {
    return getDelegate().getVertex(id);
  }

  @Override
  public void removeVertex(Vertex vertex) {
    getDelegate().removeVertex(vertex);
  }

  @Override
  public Iterable<Vertex> getVertices() {
    return getDelegate().getVertices();
  }

  @Override
  public Iterable<Vertex> getVertices(String key, Object value) {
    return getDelegate().getVertices(key, value);
  }

  @Override
  public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
    return getDelegate().addEdge(id, outVertex, inVertex, label);
  }

  @Override
  public Edge getEdge(Object id) {
    return getDelegate().getEdge(id);
  }

  @Override
  public void removeEdge(Edge edge) {
    getDelegate().removeEdge(edge);
  }

  @Override
  public Iterable<Edge> getEdges() {
    return getDelegate().getEdges();
  }

  @Override
  public Iterable<Edge> getEdges(String key, Object value) {
    return getDelegate().getEdges(key, value);
  }

  @Override
  public GraphQuery query() {
    return getDelegate().query();
  }

  @Override
  public void shutdown() {
    getDelegate().shutdown();
  }

  public AbstractGraphWrapper() {
    super();
  }

}
