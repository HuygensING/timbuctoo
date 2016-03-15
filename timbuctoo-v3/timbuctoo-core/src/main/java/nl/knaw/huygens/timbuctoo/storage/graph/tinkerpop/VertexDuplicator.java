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

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType;

import java.util.Iterator;
import java.util.Objects;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.IS_LATEST;

public class VertexDuplicator {

  private static final String VERSION_OF_LABEL = SystemRelationType.VERSION_OF.name();
  private Graph db;
  private final EdgeManipulator edgeManipulator;

  public VertexDuplicator(Graph db) {
    this(db, new EdgeManipulator());
  }

  public VertexDuplicator(Graph db, EdgeManipulator edgeManipulator) {
    this.db = db;
    this.edgeManipulator = edgeManipulator;
  }

  public Vertex duplicate(Vertex vertexToDuplicate) {
    Vertex duplicate = db.addVertex(null);

    duplicateProperties(vertexToDuplicate, duplicate);

    moveOutgoingEdges(vertexToDuplicate, duplicate);

    moveIncomingEdges(vertexToDuplicate, duplicate);

    changeLatestVertex(vertexToDuplicate, duplicate);

    return duplicate;
  }

  private void changeLatestVertex(Vertex vertexToDuplicate, Vertex duplicate) {
    duplicate.setProperty(IS_LATEST, true);
    vertexToDuplicate.setProperty(IS_LATEST, false);

    vertexToDuplicate.addEdge(VERSION_OF_LABEL, duplicate);
  }

  private void moveIncomingEdges(Vertex vertexToDuplicate, Vertex duplicate) {
    for (Iterator<Edge> iterator = vertexToDuplicate.getEdges(Direction.IN).iterator(); iterator.hasNext(); ) {
      Edge edge = iterator.next();

      if (!isVersionOfEdge(edge)) {
        edgeManipulator.changeTarget(edge, duplicate);
      }

    }
  }

  private void moveOutgoingEdges(Vertex vertexToDuplicate, Vertex duplicate) {
    for (Iterator<Edge> iterator = vertexToDuplicate.getEdges(Direction.OUT).iterator(); iterator.hasNext(); ) {
      Edge edge = iterator.next();

      if (!isVersionOfEdge(edge)) {
        edgeManipulator.changeSource(edge, duplicate);
      }

    }
  }

  private boolean isVersionOfEdge(Edge edge) {
    return Objects.equals(edge.getLabel(), VERSION_OF_LABEL);
  }

  private void duplicateProperties(Vertex vertexToDuplicate, Vertex duplicate) {
    for (String key : vertexToDuplicate.getPropertyKeys()) {
      duplicate.setProperty(key, vertexToDuplicate.getProperty(key));
    }
  }

}
