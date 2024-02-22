package nl.knaw.huygens.timbuctoo.graphql.mutations;

import graphql.schema.DataFetchingEnvironment;
import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.dropwizard.endpoints.GetEntity;
import nl.knaw.huygens.timbuctoo.dropwizard.endpoints.GetEntityInGraph;
import nl.knaw.huygens.timbuctoo.security.dto.User;
import nl.knaw.huygens.timbuctoo.redirectionservice.EntityLookup;
import nl.knaw.huygens.timbuctoo.redirectionservice.ImmutableEntityLookup;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.redirectionservice.RedirectionService;

import java.io.UnsupportedEncodingException;
import java.net.URI;

public class PersistEntityMutation extends Mutation {
  private final RedirectionService redirectionService;
  private final String dataSetId;
  private final String dataSetName;
  private final String ownerId;
  private final UriHelper uriHelper;

  public PersistEntityMutation(Runnable schemaUpdater, RedirectionService redirectionService,
                               String dataSetId, UriHelper uriHelper) {
    super(schemaUpdater);
    this.redirectionService = redirectionService;
    this.dataSetId = dataSetId;
    Tuple<String, String> dataSetIdSplit = DataSetMetaData.splitCombinedId(dataSetId);
    dataSetName = dataSetIdSplit.right();
    ownerId = dataSetIdSplit.left();
    this.uriHelper = uriHelper;
  }

  @Override
  public Object executeAction(DataFetchingEnvironment env) {
    if (redirectionService != null) {
      User user = MutationHelpers.getUser(env);
      String graph = env.getArgument("graph");
      String entityUri = env.getArgument("entityUri");
      URI uri;
      try {
        if (graph != null) {
          uri = GetEntityInGraph.makeUrl(ownerId, dataSetName, graph, entityUri);
        } else {
          uri = GetEntity.makeUrl(ownerId, dataSetName, entityUri);
        }
      } catch (UnsupportedEncodingException e) {
        return ImmutableMap.of("message", "Request for persistent Uri failed.");
      }
      URI fullUri = uriHelper.fromResourceUri(uri);

      EntityLookup entityLookup =
          ImmutableEntityLookup.builder().dataSetId(dataSetId).uri(entityUri).user(user).build();
      redirectionService.add(fullUri, entityLookup);

      return ImmutableMap.of("message", "Request for persistent Uri accepted");
    }

    return ImmutableMap.of("message", "Request for persistent Uri failed: no service configured!");
  }
}
