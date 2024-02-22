package nl.knaw.huygens.timbuctoo.dropwizard.endpoints;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.security.UserValidator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Path("/{ver:(v5/)?}{ownerId}/{dataSetId}/{id}")
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
  public Response getEntityInGraph(@Context HttpHeaders headers,
                                   @PathParam("ownerId") String ownerId,
                                   @PathParam("dataSetId") String dataSetId,
                                   @PathParam("id") String id) {
    return handleRequest(ownerId, dataSetId, headers, id, dataSet -> uri ->
            dataSet.getQuadStore().getQuads(uri).filter(quad -> quad.getDirection() == Direction.OUT));
  }
}
