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

import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.Vertex;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompositeGraphWrapperTest extends AbstractGraphWrapperTest {

  public static final Class<Vertex> ELEMENT_CLASS = Vertex.class;
  public static final String KEY = "KEY";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private Graph graph;
  private TransactionalGraph transactionalGraph;
  private KeyIndexableGraph keyIndexableGraph;
  private CompositeGraphWrapper instance;

  @Before
  public void setup() {
    graph = mock(Graph.class);
    transactionalGraph = mock(TransactionalGraph.class);
    keyIndexableGraph = mock(KeyIndexableGraph.class);

    instance = new CompositeGraphWrapper(graph, transactionalGraph, keyIndexableGraph);
  }

  @Override
  protected AbstractGraphWrapper getInstance() {
    return this.instance;
  }

  @Override
  protected Graph getDelegate() {
    return this.graph;
  }

  @Test
  public void commitDelegatesToTheTransactionalGraph() {
    // action
    instance.commit();

    // verify
    verify(transactionalGraph).commit();
  }

  @Test
  public void rollbackDelegatesToTheTransactionalGraph(){
    // action
    instance.rollback();

    // verify
    verify(transactionalGraph).rollback();
  }

  @Test
  public void stopTranstitionDelegatesToTheTransactionalGraph() {
    // action
    instance.stopTransaction(Conclusion.FAILURE);

    // verify
    transactionalGraph.stopTransaction(Conclusion.FAILURE);
  }

  @Test
  public void dropKeyIndexDelegatesToTheKeyIndexableGraph(){
    // action
    instance.dropKeyIndex(KEY, ELEMENT_CLASS);

    // verify
    keyIndexableGraph.dropKeyIndex(KEY, ELEMENT_CLASS);
  }

  @Test
  public void createKeyIndexDelegatesToTheKeyIndexableGraph(){
    // action
    instance.createKeyIndex(KEY, ELEMENT_CLASS);

    // verify
    keyIndexableGraph.createKeyIndex(KEY, ELEMENT_CLASS);
  }
  
  @Test
  public void getIndexedKeysDelegatesToTheKeyIndexableGraph(){
    // setup
    Set<String> indexedKeys = Sets.newHashSet(KEY);
    when(keyIndexableGraph.getIndexedKeys(ELEMENT_CLASS)).thenReturn(indexedKeys);

    // action
    Set<String> actualIndexedKeys = instance.getIndexedKeys(ELEMENT_CLASS);

    // verify
    assertThat(actualIndexedKeys, is(sameInstance(indexedKeys)));
  }

}
