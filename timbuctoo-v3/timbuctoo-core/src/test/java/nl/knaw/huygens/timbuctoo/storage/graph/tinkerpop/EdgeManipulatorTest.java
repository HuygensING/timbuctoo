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

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.model.DomainEntity.DB_PID_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.DB_REV_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeMockBuilder.anEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EdgeManipulatorTest {
  private static final String PID = "pid";
  private static final int REV = 2;
  private static final String LABEL = "label";
  private Vertex sourceVertex;
  private Vertex targetVertex;
  private Edge edge;
  private EdgeManipulator instance;
  private Edge duplicate;
  private Vertex newSource;
  private Edge edgeWithNewSource;
  private Vertex newTarget;
  private Edge edgeWithNewTarget;

  @Before
  public void setup() {
    targetVertex = aVertex().build();
    newTarget = aVertex().build();

    setupSource();
    setupNewSource();

    setupEdge();

    instance = new EdgeManipulator();

    duplicate = anEdge().build();
    when(sourceVertex.addEdge(LABEL, targetVertex)).thenReturn(duplicate);
  }

  private void setupSource() {
    sourceVertex = aVertex().build();
    edgeWithNewTarget = anEdge().build();
    when(sourceVertex.addEdge(LABEL, newTarget)).thenReturn(edgeWithNewTarget);
  }

  private void setupNewSource() {
    newSource = aVertex().build();
    edgeWithNewSource = anEdge().build();
    when(newSource.addEdge(LABEL, targetVertex)).thenReturn(edgeWithNewSource);
  }

  private void setupEdge() {
    edge = anEdge() //
      .withLabel(LABEL)//
      .withSource(sourceVertex)//
      .withTarget(targetVertex)//
      .withRev(REV) //
      .withPID(PID)//
      .build();
  }

  @Test
  public void duplicateLetsTheStartNodeCreateANewEdgeWithTheSameEndNodeAndSameType() {
    // action
    instance.duplicate(edge);

    // verify
    verify(sourceVertex).addEdge(LABEL, targetVertex);
  }

  @Test
  public void duplicateCopiesAllThePropertiesOfTheOriginalEdgeToTheDuplicate() {
    // action
    instance.duplicate(edge);

    // verify
    verify(duplicate).setProperty(DB_REV_PROP_NAME, REV);
    verify(duplicate).setProperty(DB_PID_PROP_NAME, PID);
  }

  @Test
  public void duplicateAddsIsLatestPropertyAndSetsItToFalseOnTheEdgeToDuplicate() {
    // action
    instance.duplicate(edge);

    // verify
    verify(edge).setProperty(ElementFields.IS_LATEST, false);
    verify(duplicate).setProperty(ElementFields.IS_LATEST, true);
  }

  @Test
  public void changeSourceCreatesANewEdgeBetweenTheNewSourceAndTheEdgesTarget() {
    // action
    instance.changeSource(edge, newSource);

    // verify
    verify(newSource).addEdge(LABEL, targetVertex);
  }

  @Test
  public void changeSourceCopiesThePropertiesOfTheEdgeToTheNewlyCreated() {
    // action
    instance.changeSource(edge, newSource);

    // verify
    verify(edgeWithNewSource).setProperty(DB_REV_PROP_NAME, REV);
    verify(edgeWithNewSource).setProperty(DB_PID_PROP_NAME, PID);
  }

  @Test
  public void changeSourceRemovesTheOriginalEdge() {
    // action
    instance.changeSource(edge, newSource);

    // verify
    verify(edge).remove();
  }

  @Test
  public void changeTargetCreatesANewEdgeBetweenTheEdgesSourceAndTheNewTarget() {
    // action
    instance.changeTarget(edge, newTarget);

    // verify
    verify(sourceVertex).addEdge(LABEL, newTarget);
  }

  @Test
  public void changeTargetCopiesThePropertiesOfTheEdgeToTheNewlyCreated() {
    // action
    instance.changeTarget(edge, newTarget);

    // verify
    verify(edgeWithNewTarget).setProperty(DB_REV_PROP_NAME, REV);
    verify(edgeWithNewTarget).setProperty(DB_PID_PROP_NAME, PID);
  }

  @Test
  public void changeTargetRemovesTheOriginalEdge() {
    // action
    instance.changeTarget(edge, newTarget);

    // verify
    verify(edge).remove();
  }

}
