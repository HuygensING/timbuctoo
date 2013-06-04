package nl.knaw.huygens.repository.resources;

import java.io.IOException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.User;

import com.google.inject.Inject;

@Path("resources/user")
public class UserResource {
  private static final String ADMIN_ROLE = "ADMIN";
  private static final String USER_PATH = "resources/user/";
  private static final String LOCATION_STRING = "location";
  private static final String ID_PARAM = "id";
  private StorageManager storageManager;

  @Inject
  public UserResource(StorageManager storageManager) {
    this.storageManager = storageManager;
  }

  @GET
  @Path("/all")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public List<User> getAll(@QueryParam("rows") @DefaultValue("200") int rows, @QueryParam("start") int start) {
    return storageManager.getAllLimited(User.class, start, rows);
  }

  @POST
  @Path("/all")
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public Response create(User user, @Context UriInfo uriInfo) throws IOException {

    storageManager.addDocument(User.class, user);

    String location = uriInfo.getBaseUri().toString() + USER_PATH + user.getId();
    return Response.status(Response.Status.CREATED).header(LOCATION_STRING, location).build();
  }

  @GET
  @Path("/{id:USR\\d+}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public User get(@PathParam(ID_PARAM) String id) {
    User user = storageManager.getDocument(User.class, id);

    if (user == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    return user;
  }

  @PUT
  @Path("/{id:USR\\d+}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public Response put(@PathParam(ID_PARAM) String id) throws IOException {
    User user = storageManager.getDocument(User.class, id);

    if (user == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    storageManager.modifyDocument(User.class, user);

    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @DELETE
  @Path("/{id:USR\\d+}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public Response delete(@PathParam(ID_PARAM) String id) throws IOException {
    User user = storageManager.getDocument(User.class, id);

    if (user == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    storageManager.removeDocument(User.class, user);

    return Response.status(Response.Status.OK).build();
  }
}
