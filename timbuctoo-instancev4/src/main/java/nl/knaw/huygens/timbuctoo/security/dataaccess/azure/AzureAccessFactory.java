package nl.knaw.huygens.timbuctoo.security.dataaccess.azure;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.util.EmptyIterator;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.table.CloudTableClient;
import nl.knaw.huygens.timbuctoo.security.dataaccess.AccessFactory;
import nl.knaw.huygens.timbuctoo.security.dataaccess.LoginAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.UserAccess;
import nl.knaw.huygens.timbuctoo.security.dataaccess.VreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.slf4j.Logger;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class AzureAccessFactory implements AccessFactory {
  private static final Logger LOG = getLogger(AzureAccessFactory.class);

  @JsonProperty
  private String connectionString;
  @JsonIgnore
  private Map<String, CloudTableClient> cloudTableClients = new HashMap<>();

  private CloudTableClient getCloudTableClient(String connectionString) throws AzureAccessNotPossibleException {
    try {
      return cloudTableClients.computeIfAbsent(connectionString, conn -> {
        try {
          return CloudStorageAccount.parse(connectionString).createCloudTableClient();
        } catch (URISyntaxException | InvalidKeyException e) {
          throw new RuntimeException(e);
        }
      });
    } catch (Exception e) {
      LOG.error("Could not construct cloud client", e.getCause());
      throw new AzureAccessNotPossibleException("Could not construct cloud client");
    }
  }


  @Override
  public LoginAccess getLoginAccess() throws AzureAccessNotPossibleException {
    return new AzureLoginAccess(getCloudTableClient(connectionString));
  }

  @Override
  public UserAccess getUserAccess() throws AzureAccessNotPossibleException {
    return new AzureUserAccess(getCloudTableClient(connectionString));
  }

  @Override
  public VreAuthorizationAccess getVreAuthorizationAccess() throws AzureAccessNotPossibleException {
    return new AzureVreAuthorizationAccess(getCloudTableClient(connectionString));
  }

  @Override
  public Iterator<Tuple<String, HealthCheck>> getHealthChecks() {
    return new EmptyIterator<>();
  }
}
