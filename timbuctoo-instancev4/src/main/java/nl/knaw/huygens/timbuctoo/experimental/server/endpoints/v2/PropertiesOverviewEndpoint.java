package nl.knaw.huygens.timbuctoo.experimental.server.endpoints.v2;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

@Path("/v2.1/system/vres/properties")
@Produces(MediaType.APPLICATION_JSON)
public class PropertiesOverviewEndpoint {

  private final TinkerpopGraphManager graphManager;
  private Vres vres;

  private static final String[] DONT_USE = {"relationtype" , "searchresult"};

  public PropertiesOverviewEndpoint(Vres vres, TinkerpopGraphManager graphManager) {
    this.vres = vres;
    this.graphManager = graphManager;
  }

  @GET
  public Response get() {
    Set<String> result = new HashSet<String>();
    List<Vertex> vertexList = graphManager.getLatestState().V().toList();
    for (Vertex vertex : vertexList) {
      Iterator<VertexProperty<Object>> allProps = vertex.properties();
      while (allProps.hasNext()) {
        VertexProperty<Object> prop = allProps.next();
        String key = prop.key();
        String[] parts = key.split("_");
        boolean useKey = true;
        if (parts.length > 1) {
          for (String nf : DONT_USE) {
            if (nf.equals(parts[0])) {
              useKey = false;
            }
          }
        }
        if (useKey) {
          boolean functional = isFunctional(parts);
          String collection = "";
          if (functional) {
            collection = vres.getCollectionForType(parts[0]).get().getCollectionName();
          }
          result.add(key + ";" + functional + ";" + collection);
        }
      }
    }
    ArrayList<String> resultList = new ArrayList<String>();
    resultList.addAll(result);
    Collections.sort(resultList);
    resultList.add(0, "PROPERTY NAME;IS FUNCTIONAL?;FOUND COLLECTION");
    return Response.ok(resultList).build();
  }

  private boolean isFunctional(String[] parts) {
    if (parts.length > 1 && parts[0].equals("tim") && parts[1].equals("id")) {
      return false;
    }
    if (parts.length == 1) {
      return false;
    }
    if (parts[parts.length - 1].equals("sort")) {
      return false;
    }
    return true ;
  }

}
