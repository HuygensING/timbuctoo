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
import com.tinkerpop.blueprints.*;

import java.util.Set;

class NonKeyIndexableGraph extends AbstractGraphWrapper implements KeyIndexableGraph {
  private Graph graph;

  public NonKeyIndexableGraph(Graph graph) {
    this.graph = graph;
  }

  @Override
  public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {
  }

  @Override
  public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, Parameter... indexParameters) {
  }

  @Override
  public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
    return Sets.newHashSet();
  }

  @Override
  protected Graph getDelegate() {
    return this.graph;
  }

}
