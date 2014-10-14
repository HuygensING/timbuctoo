package nl.knaw.huygens.timbuctoo.graph;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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
import java.util.Map;

import com.google.common.collect.Maps;

public class Vertex {

  private final String name;
  private final Map<Vertex, Edge> adjacencies;

  public Vertex(String name) {
    this.name = name;
    adjacencies = Maps.newHashMap();
  }

  public String getName() {
    return name;
  }

  public Collection<Edge> getEdges() {
    return adjacencies.values();
  }

  /**
   * Returns the edge from this vertex to the specified vertex,
   * creating a new vertex if it does not exist.
   */
  public Edge getEdgeTo(Vertex vertex) {
    Edge edge = adjacencies.get(vertex);
    if (edge == null) {
      edge = new Edge(vertex);
      adjacencies.put(vertex, edge);
    }
    return edge;
  }

}
