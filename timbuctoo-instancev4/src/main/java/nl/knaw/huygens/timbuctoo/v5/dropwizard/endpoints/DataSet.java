package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Path("/v5/{userId}/{dataSetId}")
public class DataSet {
  public static URI makeUrl(String userId, String dataSetId) {
    return UriBuilder.fromResource(DataSet.class)
                     .buildFromMap(ImmutableMap.of(
                       "userId", userId,
                       "dataSetId", dataSetId
                     ));
  }
  // TODO make an api description for the dataset
}
