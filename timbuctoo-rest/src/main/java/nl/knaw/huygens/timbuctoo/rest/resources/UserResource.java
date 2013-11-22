package nl.knaw.huygens.timbuctoo.rest.resources;

import java.io.IOException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Inject;

@Path(Paths.SYSTEM_PREFIX + "/users")
public class UserResource extends ResourceBase {

  private static final String ID_REGEX = "/{id:" + User.ID_PREFIX + "\\d+}";
  private static final String UNVERIFIED_USER_ROLE = "UNVERIFIED_USER";
  private static final String USER_ROLE = "USER";
  private static final String ADMIN_ROLE = "ADMIN";
  private static final String ID_PARAM = "id";

  private final StorageManager storageManager;
  private final MailSender mailSender;

  @Inject
  public UserResource(StorageManager storageManager, MailSender mailSender) {
    this.storageManager = storageManager;
    this.mailSender = mailSender;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public List<User> getAll(@QueryParam("rows") @DefaultValue("200") int rows, @QueryParam("start") int start) {
    return storageManager.getAllLimited(User.class, start, rows);
  }

  @GET
  @Path(ID_REGEX)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public User get(@PathParam(ID_PARAM) String id) {
    return checkNotNull(storageManager.getEntity(User.class, id), Status.NOT_FOUND);
  }

  @GET
  @Path("/me")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ADMIN_ROLE, USER_ROLE, UNVERIFIED_USER_ROLE })
  public User getMyUserData(@QueryParam("id") String id, @QueryParam("VRE_ID") String vreId) {
    User user = storageManager.getEntity(User.class, id);
    VREAuthorization example = new VREAuthorization();
    example.setUserId(id);
    example.setVreId(vreId);
    VREAuthorization vreAuthorization = storageManager.findEntity(VREAuthorization.class, example);
    user.setVreAuthorization(vreAuthorization);

    return user;
  }

  @PUT
  @Path(ID_REGEX)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public Response put(@PathParam(ID_PARAM) String id, User user) throws IOException {
    try {
      storageManager.modifyEntity(User.class, user);
    } catch (IOException ex) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }

    sendEmail(user);

    return Response.status(Status.NO_CONTENT).build();
  }

  private void sendEmail(User user) {
    StringBuilder contentbuilder = new StringBuilder("Beste ");
    contentbuilder.append(user.getFirstName());
    contentbuilder.append(",\n");
    contentbuilder.append("Je bent toegelaten tot ");
    String vreId = user.getVreId();
    contentbuilder.append(vreId);
    contentbuilder.append(". Je hebt nu de mogelijkheid om ook gegevens te wijzigen.\n");
    contentbuilder.append("Met vriendelijke groet,\n");
    contentbuilder.append("De administrator van ");
    contentbuilder.append(vreId);

    if (!StringUtils.isBlank(user.getEmail())) {
      mailSender.sendMail(user.getEmail(), "U ben toegelaten tot de VRE.", contentbuilder.toString());
    }
  }

  @DELETE
  @Path(ID_REGEX)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public Response delete(@PathParam(ID_PARAM) String id) throws IOException {
    User user = checkNotNull(storageManager.getEntity(User.class, id), Status.NOT_FOUND);

    storageManager.removeEntity(user);

    return Response.status(Status.NO_CONTENT).build();
  }

}
