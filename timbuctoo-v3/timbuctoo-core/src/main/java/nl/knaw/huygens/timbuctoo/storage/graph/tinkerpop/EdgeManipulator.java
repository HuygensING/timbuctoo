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
import com.tinkerpop.blueprints.Vertex;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.IS_LATEST;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.sourceOfEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.targetOfEdge;

public class EdgeManipulator {

  public EdgeManipulator() {
    // TODO Auto-generated constructor stub
  }

  public void duplicate(Edge edgeToDuplicate) {
    Edge duplicate = createDuplicate(edgeToDuplicate);

    addProperties(edgeToDuplicate, duplicate);

    changeLatest(duplicate, edgeToDuplicate);
  }

  private void changeLatest(Edge duplicate, Edge edgeToDuplicate) {
    duplicate.setProperty(IS_LATEST, true);
    edgeToDuplicate.setProperty(IS_LATEST, false);
  }

  private Edge createDuplicate(Edge edgeToDuplicate) {
    Vertex soureOfEdge = edgeToDuplicate.getVertex(Direction.OUT);
    Vertex targetOfEdge = edgeToDuplicate.getVertex(Direction.IN);

    Edge duplicate = soureOfEdge.addEdge(edgeToDuplicate.getLabel(), targetOfEdge);
    return duplicate;
  }

  private void addProperties(Edge edgeToDuplicate, Edge duplicate) {
    for (String key : edgeToDuplicate.getPropertyKeys()) {
      duplicate.setProperty(key, edgeToDuplicate.getProperty(key));
    }
  }

  public void changeSource(Edge original, Vertex newSource) {
    Edge newEdge = newSource.addEdge(original.getLabel(), targetOfEdge(original));

    addProperties(original, newEdge);

    original.remove();
  }

  public void changeTarget(Edge original, Vertex newTarget) {
    Edge newEdge = sourceOfEdge(original).addEdge(original.getLabel(), newTarget);

    addProperties(original, newEdge);

    original.remove();
  }
}
