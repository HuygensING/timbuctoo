package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.USER_ID_KEY;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.UNVERIFIED_USER_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.USER_ROLE;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.security.UserRoles;
import nl.knaw.huygens.timbuctoo.storage.Repository;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Inject;

@Path(Paths.SYSTEM_PREFIX + "/" + Paths.USER_PATH)
public class UserResource extends ResourceBase {

  private static final String ID_REGEX = "/{id:" + User.ID_PREFIX + "\\d+}";
  private static final String VRE_AUTHORIZATION_COLLECTION_PATH = ID_REGEX + "/vreauthorizations";
  private static final String VRE_AUTHORIZATION_PATH = VRE_AUTHORIZATION_COLLECTION_PATH + "/{vre: \\w+}";
  private static final String ID_PARAM = "id";

  private final Repository repository;
  private final MailSender mailSender;

  @Inject
  public UserResource(Repository repository, MailSender mailSender) {
    this.repository = repository;
    this.mailSender = mailSender;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public List<User> getAll(@QueryParam("rows") @DefaultValue("200") int rows, @QueryParam("start") int start) {
    return repository.getAllLimited(User.class, start, rows);
  }

  @GET
  @Path(ID_REGEX)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public User get(@PathParam(ID_PARAM) String id) {
    User user = repository.getEntity(User.class, id);
    checkNotNull(user, Status.NOT_FOUND, "No User with id %s", id);
    return user;
  }

  @GET
  @Path("/me")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ADMIN_ROLE, USER_ROLE, UNVERIFIED_USER_ROLE })
  public User getMyUserData(@QueryParam(USER_ID_KEY) String id, @QueryParam("VRE_ID") String vreId) {
    User user = repository.getEntity(User.class, id);
    checkNotNull(user, Status.NOT_FOUND, "No User with id %s", id);

    VREAuthorization example = new VREAuthorization(vreId, id);
    VREAuthorization authorization = repository.findEntity(VREAuthorization.class, example);

    user.setVreAuthorization(authorization);
    return user;
  }

  @PUT
  @Path(ID_REGEX)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public Response put(@PathParam(ID_PARAM) String id, User user) {
    try {
      repository.updateSystemEntity(User.class, user);
    } catch (StorageException e) {
      throw new TimbuctooException(Status.NOT_FOUND, "User %s not found", id);
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
  public Response delete(@PathParam(ID_PARAM) String id) throws StorageException {
    User user = repository.getEntity(User.class, id);
    checkNotNull(user, Status.NOT_FOUND, "No User with id %s", id);
    repository.deleteSystemEntity(user);
    return Response.status(Status.NO_CONTENT).build();
  }

  // VREAuthorization

  @GET
  @Path(VRE_AUTHORIZATION_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public VREAuthorization getVreAuthorization(//
      @PathParam(ID_PARAM) String userId, //
      @PathParam("vre") String vreId,//
      @HeaderParam(VRE_ID_KEY) String userVREId//
  ) {
    checkIfInScope(vreId, userVREId);
    return findVREAuthorization(vreId, userId);
  }

  @POST
  @Path(VRE_AUTHORIZATION_COLLECTION_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public Response postVREAuthorization(//
      @PathParam("id") String userId,//
      @HeaderParam(VRE_ID_KEY) String userVREId,//
      VREAuthorization authorization//
  ) throws URISyntaxException, StorageException, ValidationException {

    checkNotNull(authorization, Status.BAD_REQUEST, "Missing VREAuthorization");
    checkIfInScope(authorization.getVreId(), userVREId);

    String vreId = authorization.getVreId();
    repository.addSystemEntity(VREAuthorization.class, authorization);

    return Response.created(new URI(vreId)).build();
  }

  @PUT
  @Path(VRE_AUTHORIZATION_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public void putVREAUthorization(//
      @PathParam("id") String userId,//
      @PathParam("vre") String vreId,//
      @HeaderParam(VRE_ID_KEY) String userVREId,//
      VREAuthorization authorization//
  ) throws StorageException {

    checkNotNull(authorization, Status.BAD_REQUEST, "Missing VREAuthorization");
    checkIfInScope(vreId, userVREId);
    findVREAuthorization(vreId, userId);
    repository.updateSystemEntity(VREAuthorization.class, authorization);
  }

  @DELETE
  @Path(VRE_AUTHORIZATION_PATH)
  @RolesAllowed(ADMIN_ROLE)
  public void deleteVREAuthorization(//
      @PathParam(ID_PARAM) String userId,// 
      @PathParam("vre") String vreId,//
      @HeaderParam(VRE_ID_KEY) String userVREId//
  ) throws StorageException {
    checkIfInScope(vreId, userVREId);
    VREAuthorization authorization = findVREAuthorization(vreId, userId);
    repository.deleteSystemEntity(authorization);
  }

  /**
   * Checks if the user that is logged in in the VRE of {@code userVREID}, 
   * is allowed to access the {@code VREAuthorization} of the VRE of {@code vreId}.
   * @param vreId the id of the VRE the user want to access {@code VREAuthorization} of.
   * @param userVREId the id of the VRE the user is currently logged in to.
   * @throws a {@link TimbuctooException} with a {@code FORBIDDEN} status.
   */
  private void checkIfInScope(String vreId, String userVREId) {
    if (!StringUtils.equals(vreId, userVREId)) {
      throw new TimbuctooException(Status.FORBIDDEN, "VRE %s has no permission to edit VREAuthorizations of VRE %s", userVREId, vreId);
    }
  }

  private VREAuthorization findVREAuthorization(String vreId, String userId) {
    VREAuthorization example = new VREAuthorization(vreId, userId);
    VREAuthorization authorization = repository.findEntity(VREAuthorization.class, example);
    checkNotNull(authorization, Status.NOT_FOUND, "Missing VREAuthorization for userId %s and vreId %s", userId, vreId);
    return authorization;
  }

  // Roles

  @GET
  @Path("roles")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoles.ADMIN_ROLE)
  public List<String> getRoles() {
    return UserRoles.getAll();
  }

}
