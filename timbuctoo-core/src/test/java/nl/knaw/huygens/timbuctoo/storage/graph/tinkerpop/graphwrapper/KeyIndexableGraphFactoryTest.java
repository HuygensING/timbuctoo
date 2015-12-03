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

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class KeyIndexableGraphFactoryTest {

  private CompositeGraphWrapper compositeGraphWrapper;
  private KeyIndexableGraphFactory instance;

  @Before
  public void setup() {
    compositeGraphWrapper = mock(CompositeGraphWrapper.class);

    instance = new KeyIndexableGraphFactory();
  }

  @Test
  public void wrapReturnsTheGraphWhenItIsAKeyIndexableGraph() {
    // setup
    KeyIndexableGraph graph = mock(KeyIndexableGraph.class);

    // action
    KeyIndexableGraph returnedGraph = instance.create(graph);

    // verify
    assertThat(returnedGraph, is(sameInstance(graph)));
  }

  @Test
  public void wrapReturnsANoOpKeyIndexableGraphWrapperWhenItIsANotKeyIndexableGraph() {
    // setup
    Graph graph = mock(Graph.class);

    // action
    KeyIndexableGraph returnedGraph = instance.create(graph);

    // verify
    assertThat(returnedGraph, is(instanceOf(NonKeyIndexableGraph.class)));
  }

}
