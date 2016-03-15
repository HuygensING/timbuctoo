package nl.knaw.huygens.timbuctoo.server.rest;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.slf4j.Logger;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

import static org.apache.tinkerpop.gremlin.structure.Direction.IN;
import static org.apache.tinkerpop.gremlin.structure.Direction.OUT;

@Path("/v2.1/gremlin")
@Produces(MediaType.TEXT_PLAIN)
public class DirectQueryEndpoint {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DirectQueryEndpoint.class);

  private final GremlinGroovyScriptEngine engine;
  private final GraphWrapper wrapper;
  private final Bindings bindings;

  public DirectQueryEndpoint(GraphWrapper wrapper) {
    this.wrapper = wrapper;
    this.engine = new GremlinGroovyScriptEngine();
    this.bindings = engine.createBindings();
  }

  @POST
  @Consumes("text/plain")
  public Response post(String query) {
    bindings.put("g", wrapper.getGraph().traversal());
    bindings.put("maria", wrapper.getGraph().traversal().V().has("tim_id", "077bf0b5-6b7d-45aa-89ff-6ecf2cfc549c"));

    try {
      return Response.ok(evaluateQuery(query)).build();
    } catch (ScriptException e) {
      LOG.error(e.getMessage(), e);
      return Response.status(500).entity(e.getMessage()).build();
    }
  }


  @GET
  public Response get(@QueryParam("query") String query) {
    if (Strings.isNullOrEmpty(query)) {
      String usageMessage = "Usage: ?query=g.V().has(\"tim_id\", \"077bf0b5-6b7d-45aa-89ff-6ecf2cfc549c\")" +
        ".has(\"isLatest\", true).properties()\n" +
        "or ?query=maria.out()";
      return Response.ok(usageMessage).build();
    }
    bindings.put("g", wrapper.getGraph().traversal());
    bindings.put("maria", wrapper.getGraph().traversal().V().has("tim_id", "077bf0b5-6b7d-45aa-89ff-6ecf2cfc549c"));

    try {
      return Response.ok(evaluateQuery(query)).build();
    } catch (ScriptException e) {
      LOG.error(e.getMessage(), e);
      return Response.status(500).entity(e.getMessage()).build();
    }
  }

  private String evaluateQuery(String query) throws ScriptException {
    GraphTraversal traversalResult = (GraphTraversal) engine.eval(query, bindings);
    StringBuilder result = new StringBuilder();
    while (traversalResult.hasNext()) {
      Object item = traversalResult.next();
      if (item instanceof Vertex) {
        Vertex vertex = (Vertex) item;
        result.append(String.format("Vertex [%s]:\n", vertex.id()));
        ArrayList<VertexProperty<Object>> properties = Lists.newArrayList(vertex.properties());
        properties.sort((o1, o2) -> java.text.Collator.getInstance().compare(o1.label(), o2.label()));
        for (VertexProperty<Object> property : properties) {
          result.append(String.format("  %s: %s\n", property.label(), property.value()));
        }
        for (Edge edge : ImmutableList.copyOf(vertex.edges(IN))) {
          result.append(String.format("  <--[%s]-- v[%s]\n", edge.label(), edge.outVertex().id()));
        }
        for (Edge edge : ImmutableList.copyOf(vertex.edges(OUT))) {
          result.append(String.format("  --[%s]--> v[%s]\n", edge.label(), edge.inVertex().id()));
        }
      } else if (item instanceof Edge) {
        Edge edge = (Edge) item;
        result.append(String.format("v[%s] --[%s]--> v[%s]\n",
                edge.inVertex().id(), edge.label(), edge.outVertex().id()));
      } else {
        result.append(item);
        result.append("\n");
      }
    }
    return result.toString();
  }

}
