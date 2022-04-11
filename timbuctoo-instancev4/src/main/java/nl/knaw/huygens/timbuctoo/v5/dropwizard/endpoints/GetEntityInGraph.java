package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;
import nl.knaw.huygens.timbuctoo.v5.util.Graph;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Path("/v5/{ownerId}/{dataSetId}/{graph}/{id}")
public class GetEntityInGraph extends AbstractGetEntity {
  public GetEntityInGraph(DataSetRepository dataSetRepository, UserValidator userValidator) {
    super(dataSetRepository, userValidator);
  }

  public static URI makeUrl(String ownerId, String dataSetId, String graph, String id)
      throws UnsupportedEncodingException {
    return UriBuilder.fromResource(GetEntityInGraph.class)
                     .buildFromMap(ImmutableMap.of(
                         "ownerId", ownerId,
                         "dataSetId", dataSetId,
                         "graph", escapeCharacters(URLEncoder.encode(graph, StandardCharsets.UTF_8)),
                         "id", escapeCharacters(URLEncoder.encode(id, StandardCharsets.UTF_8))
                     ));
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response getEntity(@HeaderParam("authorization") String authHeader,
                            @PathParam("ownerId") String ownerId,
                            @PathParam("dataSetId") String dataSetId,
                            @PathParam("graph") String graph,
                            @PathParam("id") String id) {
    return handleRequest(authHeader, ownerId, dataSetId,
      dataSet -> dataSet.getQuadStore().getQuadsInGraph(id, Optional.of(new Graph(graph))));
  }
}
