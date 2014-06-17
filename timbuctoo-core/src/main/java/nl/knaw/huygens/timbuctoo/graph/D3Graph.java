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

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Force graph.
 */
public class D3Graph {

  private final List<D3Node> nodes;
  private final Map<String, Integer> nodeMap;

  private final List<D3Link> links;

  public D3Graph() {
    nodes = Lists.newArrayList();
    nodeMap = Maps.newHashMap();
    links = Lists.newArrayList();
  }

  public int addNode(D3Node node) {
    String key = node.getkey();
    Integer index = nodeMap.get(key);
    if (index == null) {
      index = nodes.size();
      nodeMap.put(key, index);
      nodes.add(node);
    }
    return index;
  }

  public void addLink(D3Link link) {
    links.add(link);
  }

  public void addLink(int source, int target, String type) {
    addLink(new D3Link(source, target, type));
  }

  public List<D3Node> getNodes() {
    return nodes;
  }

  public int nodeCount() {
    return nodes.size();
  }

  public List<D3Link> getLinks() {
    return links;
  }

  public int linkCount() {
    return links.size();
  }

}
