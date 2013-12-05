package nl.knaw.huygens.timbuctoo.rest.resources;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.inject.Inject;

@Path(Paths.SYSTEM_PREFIX + "/relationtypes")
public class RelationTypeResource extends ResourceBase {

  private static final String ID_PARAM = "id";
  private static final String ID_PATH = "/{id: " + RelationType.ID_PREFIX + "\\d+}";

  private final StorageManager storageManager;

  @Inject
  public RelationTypeResource(StorageManager storageManager) {
    this.storageManager = storageManager;
  }

  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public List<RelationType> getAllDocs( //
      @QueryParam("rows") @DefaultValue("200") int rows, //
      @QueryParam("start") int start //
  ) {
    return storageManager.getAllLimited(RelationType.class, start, rows);
  }

  @GET
  @Path(ID_PATH)
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  @JsonView(JsonViews.WebView.class)
  public RelationType getDoc( //
      @PathParam(ID_PARAM) String id //
  ) {
    return checkNotNull(storageManager.getEntity(RelationType.class, id), Status.NOT_FOUND);
  }

}
