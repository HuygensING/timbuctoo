package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.security.UserValidator;

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

@Path("/v5/{ownerId}/{dataSetId}/{id}")
public class GetEntity extends AbstractGetEntity {
  public GetEntity(DataSetRepository dataSetRepository, UserValidator userValidator) {
    super(dataSetRepository, userValidator);
  }

  public static URI makeUrl(String ownerId, String dataSetId, String id) throws UnsupportedEncodingException {
    return UriBuilder.fromResource(GetEntity.class)
                     .buildFromMap(ImmutableMap.of(
                         "ownerId", ownerId,
                         "dataSetId", dataSetId,
                         "id", escapeCharacters(URLEncoder.encode(id, StandardCharsets.UTF_8))
                     ));
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response getEntityInGraph(@HeaderParam("authorization") String authHeader,
                                   @PathParam("ownerId") String ownerId,
                                   @PathParam("dataSetId") String dataSetId,
                                   @PathParam("id") String id) {
    return handleRequest(authHeader, ownerId, dataSetId, dataSet -> dataSet.getQuadStore().getQuads(id));
  }
}
