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
import com.tinkerpop.blueprints.TransactionalGraph;

public class GraphWrapperFactory {

  private TransactionalGraphFactory transactionGraphWrapperFactory;
  private KeyIndexableGraphFactory keyIndexableGraphFactory;

  public GraphWrapperFactory(){
    this(new TransactionalGraphFactory(), new KeyIndexableGraphFactory());
  }

  public GraphWrapperFactory(TransactionalGraphFactory transactionalGraphFactory, KeyIndexableGraphFactory keyIndexableGraphFactory) {
    this.transactionGraphWrapperFactory = transactionalGraphFactory;
    this.keyIndexableGraphFactory = keyIndexableGraphFactory;
  }

  public GraphWrapper wrap(Graph graph) {
    TransactionalGraph transactionalGraph = transactionGraphWrapperFactory.create(graph);
    KeyIndexableGraph keyIndexableGraph = keyIndexableGraphFactory.create(graph);

    CompositeGraphWrapper graphWrapper = new CompositeGraphWrapper(graph, transactionalGraph, keyIndexableGraph);

    return graphWrapper;
  }

}
