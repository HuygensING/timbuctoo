package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
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

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.graph.Edge;
import nl.knaw.huygens.timbuctoo.graph.Graph;
import nl.knaw.huygens.timbuctoo.graph.ReceptionGraphBuilder;
import nl.knaw.huygens.timbuctoo.graph.Vertex;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.rest.graph.D3Graph;
import nl.knaw.huygens.timbuctoo.rest.graph.D3Node;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

@Path("receptiongraph")
public class ReceptionGraphResource extends ResourceBase {

  private final Repository repository;

  @Inject
  public ReceptionGraphResource(Repository repository) {
    this.repository = repository;
  }

  // --- API -----------------------------------------------------------

  /**
   * Returns the reception graph for the person with the specified id,
   * the receptions being defined by the specified VRE.
   * If {@code isSubject} is {@code true} the receptions on the specified
   * person are used; otherwise the receptions authored by the specified
   * person are used.
   */
  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  public Object getGraph( //
      @QueryParam("vreId") String vreId, //
      @QueryParam("personId") String personId, //
      @QueryParam("isSubject") @DefaultValue("true") boolean isSubject) //
  {
    checkNotNull(vreId, BAD_REQUEST, "No query parameter 'vreId'");
    VRE vre = getValidVRE(repository, vreId);

    checkNotNull(personId, BAD_REQUEST, "No query parameter 'personId'");
    Person person = repository.getEntity(Person.class, personId);
    checkNotNull(person, NOT_FOUND, "No person with id %s", personId);

    try {
      ReceptionGraphBuilder builder = new ReceptionGraphBuilder(repository, vre);
      builder.addPerson(person, isSubject);
      Graph graph = builder.getGraph();
      return convertGraph(graph);
    } catch (Exception e) {
      throw new TimbuctooException(INTERNAL_SERVER_ERROR);
    }
  }

  // this is a first attempt
  // it should give a graph that is displayable
  // but it doesn't carry the proper data yet
  private D3Graph convertGraph(Graph graph) {
    D3Graph d3Graph = new D3Graph();

    Map<String, Integer> map = Maps.newHashMap();
    for (Vertex vertex : graph.getVertices()) {
      String personId = vertex.getName();
      Person person = repository.getEntity(Person.class, personId);
      D3Node node = createNode(person);
      int index = d3Graph.addNode(node);
      map.put(personId, index);
    }

    for (Vertex vertex : graph.getVertices()) {
      int source = map.get(vertex.getName());
      for (Edge edge : vertex.getEdges()) {
        int target = map.get(edge.getDestVertex().getName());
        d3Graph.addLink(source, target, edge.getWeight());
      }
    }
    return d3Graph;
  }

  private <T extends Person> D3Node createNode(T entity) {
    @SuppressWarnings("unchecked")
    Class<T> type = (Class<T>) entity.getClass();
    String key = TypeNames.getExternalName(type) + "/" + entity.getId();
    String iname = TypeNames.getInternalName(type);
    String label = entity.getDisplayName();
    D3Node node = new D3Node(key, iname, label);
    node.addDataItem("gender", entity.getGender());
    return node;
  }

}
