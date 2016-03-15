package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import static nl.knaw.huygens.timbuctoo.config.Paths.SYSTEM_PREFIX;
import static nl.knaw.huygens.timbuctoo.config.Paths.USER_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.V1_PATH_OPTIONAL;
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

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.security.UserConfigurationHandler;
import nl.knaw.huygens.timbuctoo.security.UserRoles;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Inject;

@Path(V1_PATH_OPTIONAL + SYSTEM_PREFIX + "/" + USER_PATH)
public class UserResource extends ResourceBase {

  protected static final String ID_REGEX = "/{id:" + User.ID_PREFIX + Paths.ID_REGEX + "}";
  protected static final String VRE_AUTHORIZATION_COLLECTION_PATH = ID_REGEX + "/vreauthorizations";
  protected static final String VRE_AUTHORIZATION_PATH = VRE_AUTHORIZATION_COLLECTION_PATH + "/{vre: \\w+}";
  protected static final String ID_PARAM = "id";

  protected final UserConfigurationHandler userConfigurationHandler;
  private final MailSender mailSender;

  @Inject
  public UserResource(Repository repository, UserConfigurationHandler userConfigurationHandler, MailSender mailSender, VRECollection vreCollection) {
    super(repository, vreCollection);
    this.userConfigurationHandler = userConfigurationHandler;
    this.mailSender = mailSender;
  }

  @APIDesc("Get all users. Query params: \"rows\" (default: 200), \"start\" (default: 0)")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public List<User> getAll(@QueryParam("rows") @DefaultValue("200") int rows, @QueryParam("start") int start) {
    return userConfigurationHandler.getUsers().skip(start).getSome(rows);
  }

  @APIDesc("Get a user by id")
  @GET
  @Path(ID_REGEX)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public User get(@PathParam(ID_PARAM) String id) {
    User user = userConfigurationHandler.getUser(id);
    checkNotNull(user, Status.NOT_FOUND, "No User with id %s", id);
    return user;
  }

  @APIDesc("Get the user information of the logged in user.")
  @GET
  @Path("/me")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ADMIN_ROLE, USER_ROLE, UNVERIFIED_USER_ROLE })
  public User getMyUserData(@QueryParam(USER_ID_KEY) String id, @QueryParam(VRE_ID_KEY) String vreId) {
    User user = userConfigurationHandler.getUser(id);
    checkNotNull(user, Status.NOT_FOUND, "No User with id %s", id);

    VREAuthorization example = new VREAuthorization(vreId, id);
    VREAuthorization authorization = userConfigurationHandler.findVREAuthorization(example);

    user.setVreAuthorization(authorization);
    return user;
  }

  @APIDesc("Update a user information")
  @PUT
  @Path(ID_REGEX)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public Response put(@PathParam(ID_PARAM) String id, User user) {
    try {
      userConfigurationHandler.updateUser(user);
    } catch (StorageException e) {
      throw new TimbuctooException(Status.NOT_FOUND, "User %s not found", id);
    }

    sendEmail(user);

    return Response.status(Status.NO_CONTENT).build();
  }

  protected void sendEmail(User user) {
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

  /*
   * User delete temporarily not allowed, we need to re-think, this concept.
   */
  //  @DELETE
  //  @Path(ID_REGEX)
  //  @Produces(MediaType.APPLICATION_JSON)
  //  @RolesAllowed(ADMIN_ROLE)
  //  public Response delete(@PathParam(ID_PARAM) String id) throws StorageException {
  //    User user = userConfigurationHandler.getUser(id);
  //    checkNotNull(user, Status.NOT_FOUND, "No User with id %s", id);
  //    userConfigurationHandler.deleteUser(user);
  //    return Response.status(Status.NO_CONTENT).build();
  //  }

  // VREAuthorization

  @APIDesc("Get the vre authorization of the user for a certain vre.")
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

  @APIDesc("Create a vre authorization for a certain user. Expects a vre authorization body.")
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
    userConfigurationHandler.addVREAuthorization(authorization);

    return Response.created(new URI(vreId)).build();
  }

  @APIDesc("Update a vre authorization for a certain user. Expects a vre authorization body.")
  @PUT
  @Path(VRE_AUTHORIZATION_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public Response putVREAUthorization(//
      @PathParam("id") String userId,//
      @PathParam("vre") String vreId,//
      @HeaderParam(VRE_ID_KEY) String userVREId,//
      VREAuthorization authorization//
  ) throws StorageException {

    checkNotNull(authorization, Status.BAD_REQUEST, "Missing VREAuthorization");
    checkIfInScope(vreId, userVREId);
    findVREAuthorization(vreId, userId);
    userConfigurationHandler.updateVREAuthorization(authorization);

    return Response.noContent().build();
  }

  @APIDesc("Remove a vre authorization from a certain user.")
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
    userConfigurationHandler.deleteVREAuthorization(authorization);
  }

  /**
   * Checks if the user that is logged in in the VRE of {@code userVREID}, 
   * is allowed to access the {@code VREAuthorization} of the VRE of {@code vreId}.
   * @param vreId the id of the VRE the user want to access {@code VREAuthorization} of.
   * @param userVREId the id of the VRE the user is currently logged in to.
   * @throws a {@link TimbuctooException} with a {@code FORBIDDEN} status.
   */
  protected void checkIfInScope(String vreId, String userVREId) {
    if (!StringUtils.equals(vreId, userVREId)) {
      throw new TimbuctooException(Status.FORBIDDEN, "VRE %s has no permission to edit VREAuthorizations of VRE %s", userVREId, vreId);
    }
  }

  protected VREAuthorization findVREAuthorization(String vreId, String userId) {
    VREAuthorization example = new VREAuthorization(vreId, userId);
    VREAuthorization authorization = userConfigurationHandler.findVREAuthorization(example);
    checkNotNull(authorization, Status.NOT_FOUND, "Missing VREAuthorization for userId %s and vreId %s", userId, vreId);
    return authorization;
  }

  // Roles

  @APIDesc("Return all the know roles.")
  @GET
  @Path("roles")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoles.ADMIN_ROLE)
  public List<String> getRoles() {
    return UserRoles.getAll();
  }

}
