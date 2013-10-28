package nl.knaw.huygens.timbuctoo.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.rest.util.Status;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import com.google.inject.Inject;

@Path(Paths.SYSTEM_PREFIX + "/status")
public class StatusResource {

  private final StorageManager storageManager;
  private final IndexManager indexManager;

  @Inject
  public StatusResource(StorageManager storageManager, IndexManager indexManager) {
    this.storageManager = storageManager;
    this.indexManager = indexManager;
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON })
  @APIDesc("Returns the status of the webapp.")
  public Status getStatus() {
    Status status = new Status();
    status.setStorageStatus(storageManager.getStatus());
    status.setIndexStatus(indexManager.getStatus());
    return status;
  }

}
