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
import static nl.knaw.huygens.timbuctoo.config.Paths.V2_PATH;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.USER_ID_KEY;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.UNVERIFIED_USER_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.USER_ROLE;

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
import nl.knaw.huygens.timbuctoo.mail.MailSender;
import nl.knaw.huygens.timbuctoo.model.User;
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.security.UserConfigurationHandler;
import nl.knaw.huygens.timbuctoo.security.UserRoles;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;

import com.google.inject.Inject;

@Path(V2_PATH + "/" + SYSTEM_PREFIX + "/" + USER_PATH)
public class UserResourceV2 extends UserResource {

  @Inject
  public UserResourceV2(Repository repository, UserConfigurationHandler userConfigurationHandler, MailSender mailSender, VRECollection vreCollection) {
    super(repository, userConfigurationHandler, mailSender, vreCollection);
  }

  @Override
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public List<User> getAll(@QueryParam("rows") @DefaultValue("200") int rows, @QueryParam("start") int start) {
    return super.getAll(rows, start);
  }

  @Override
  @GET
  @Path(ID_REGEX)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public User get(@PathParam(ID_PARAM) String id) {
    return super.get(id);
  }

  @Override
  @GET
  @Path("/me")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ ADMIN_ROLE, USER_ROLE, UNVERIFIED_USER_ROLE })
  public User getMyUserData(@QueryParam(USER_ID_KEY) String id, @QueryParam(VRE_ID_KEY) String vreId) {
    return super.getMyUserData(id, vreId);
  }

  @Override
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

    return Response.ok(userConfigurationHandler.getUser(id)).build();
  }

  @Override
  @GET
  @Path(VRE_AUTHORIZATION_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public VREAuthorization getVreAuthorization(//
      @PathParam(ID_PARAM) String userId, //
      @PathParam("vre") String vreId,//
      @HeaderParam(VRE_ID_KEY) String userVREId//
  ) {
    return super.getVreAuthorization(userId, vreId, userVREId);
  }

  @Override
  @POST
  @Path(VRE_AUTHORIZATION_COLLECTION_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(ADMIN_ROLE)
  public Response postVREAuthorization(//
      @PathParam("id") String userId,//
      @HeaderParam(VRE_ID_KEY) String userVREId,//
      VREAuthorization authorization//
  ) throws URISyntaxException, StorageException, ValidationException {
    return super.postVREAuthorization(userId, userVREId, authorization);
  }

  @Override
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

    return Response.ok(findVREAuthorization(userVREId, userId)).build();
  }

  @Override
  @DELETE
  @Path(VRE_AUTHORIZATION_PATH)
  @RolesAllowed(ADMIN_ROLE)
  public void deleteVREAuthorization(//
      @PathParam(ID_PARAM) String userId,// 
      @PathParam("vre") String vreId,//
      @HeaderParam(VRE_ID_KEY) String userVREId//
  ) throws StorageException {
    super.deleteVREAuthorization(userId, vreId, userVREId);
  }

  @Override
  @GET
  @Path("roles")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoles.ADMIN_ROLE)
  public List<String> getRoles() {
    return super.getRoles();
  }
}
