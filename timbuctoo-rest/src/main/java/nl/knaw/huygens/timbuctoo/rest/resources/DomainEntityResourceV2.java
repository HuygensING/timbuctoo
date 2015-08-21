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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.annotations.APIDesc;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.net.URISyntaxException;
import java.util.List;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static nl.knaw.huygens.timbuctoo.config.Paths.DOMAIN_PREFIX;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PARAM;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.ID_PARAM;
import static nl.knaw.huygens.timbuctoo.config.Paths.ID_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.V2_OR_V2_1_PATH;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.REVISION_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.USER_ID_KEY;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.USER_ROLE;

@Path(V2_OR_V2_1_PATH +  DOMAIN_PREFIX + "/" + ENTITY_PATH)
public class DomainEntityResourceV2 extends DomainEntityResource {
  private static Logger LOG = LoggerFactory.getLogger(DomainEntityResourceV2.class);

  @Inject
  public DomainEntityResourceV2(TypeRegistry registry, Repository repository, ChangeHelper changeHelper, VRECollection vreCollection) {
    super(registry, repository, changeHelper, vreCollection);
  }

  @APIDesc("Get an number of entities. Query params: \"rows\" (default: 200) and \"start\" (default: 0).")
  @Override
  @GET
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  public Response getEntities( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @QueryParam("type") String typeValue, //
      @QueryParam("rows") @DefaultValue("200") int rows, //
      @QueryParam("start") @DefaultValue("0") int start //
  ) {
    return super.getEntities(entityName, typeValue, rows, start);
  }

  /**
   * Returns a list of domain entities with a defined set of fields, currently
   * implemented in the {@code DomainEntity.getClientRepresentation) method.
   */
  @GET
  @Path("/defined")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response getEntitiesRestricted( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @QueryParam("type") String typeValue, //
      @QueryParam("rows") @DefaultValue("200") int rows, //
      @QueryParam("start") @DefaultValue("0") int start //
  ) {
    Class<? extends DomainEntity> entityType = getValidEntityType(entityName);
    List<DomainEntityDTO> dtos = retrieveDTOs(entityType, typeValue, rows, start);
    return Response.ok(new GenericEntity<List<DomainEntityDTO>>(dtos) {}).build();
  }

  private <T extends DomainEntity> List<DomainEntityDTO> retrieveDTOs(Class<T> entityType, String typeValue, int rows, int start) {
    return createRefs(entityType, retrieveEntities(entityType, typeValue, rows, start));
  }

  private <T extends DomainEntity> List<DomainEntityDTO> createRefs(Class<T> type, List<T> entities) {
    String itype = TypeNames.getInternalName(type);
    String xtype = TypeNames.getExternalName(type);
    List<DomainEntityDTO> list = Lists.newArrayListWithCapacity(entities.size());
    for (T entity : entities) {
      list.add(new DomainEntityDTO(itype, xtype, entity));
    }
    return list;
  }

  @APIDesc("Post an entity. Body required.")
  @Override
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({ USER_ROLE, ADMIN_ROLE })
  public <T extends DomainEntity> Response post( //
      @PathParam(ENTITY_PARAM) String entityName, //
      DomainEntity input, //
      @Context UriInfo uriInfo, //
      @HeaderParam(VRE_ID_KEY) String vreId, //
      @QueryParam(USER_ID_KEY) String userId//
  ) throws StorageException, URISyntaxException {
    return super.post(entityName, input, uriInfo, vreId, userId);
  }

  @APIDesc("Get a single entity. Query param: \"rev\" (default:latest) ")
  @Override
  @GET
  @Path(ID_PATH)
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
  public DomainEntity getDoc( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @PathParam(ID_PARAM) String id, //
      @QueryParam(REVISION_KEY) Integer revision//
  ) {
    Class<? extends DomainEntity> type = getValidEntityType(entityName);

    // to not break the other API's and make this one throw an exception when the variation does not exist.
    try {
      if (!repository.doesVariationExist(type, id)) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
    } catch (StorageException e) {
      throw new WebApplicationException(e);
    }

    return super.getDoc(entityName, id, revision);
  }

  @APIDesc("Update an entity. Body required.")
  @Override
  @PUT
  @Path(ID_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({ USER_ROLE, ADMIN_ROLE })
  public <T extends DomainEntity> Response put( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @PathParam(ID_PARAM) String id, //
      DomainEntity input, //
      @HeaderParam(VRE_ID_KEY) String vreId,//
      @QueryParam(USER_ID_KEY) String userId//
  ) {
    super.put(entityName, id, input, vreId, userId);

    return Response.ok(repository.getEntityOrDefaultVariationWithRelations(typeRegistry.getTypeForXName(entityName), id)).build();
  }


  @APIDesc("Delete an specific entity.")
  @Override
  @DELETE
  @Path(ID_PATH)
  @RolesAllowed({ USER_ROLE, ADMIN_ROLE })
  public Response delete( //
      @PathParam(ENTITY_PARAM) String entityName, //
      @PathParam(ID_PARAM) String id, //
      @HeaderParam(VRE_ID_KEY) String vreId) {

    Class<? extends DomainEntity> type = getValidEntityType(entityName);

    if (TypeRegistry.isPrimitiveDomainEntity(type)) {
      return Response.status(Status.BAD_REQUEST).entity("Primitive DomainEntities cannot be deleted at this moment.").build();
    }

    if (Relation.class.isAssignableFrom(type)) {
      return Response.status(Status.BAD_REQUEST).entity("Relations cannot be deleted at this moment. Use PUT with \"^accepted\" set to false.").build();
    }

    VRE vre = getValidVRE(vreId);
    checkCondition(vre.inScope(type, id), FORBIDDEN, "Entity %s %s not in scope %s", type, id, vreId);

    DomainEntity entity = repository.getEntityOrDefaultVariation(type, id);

    if (entity == null) {
      return returnNotFoundResponse(id);
    }

    try {
      List<String> updatedRelationIds = repository.deleteDomainEntity(entity);
      changeHelper.notifyChange(ActionType.MOD, type, entity, id);
      try {
        // FIXME: Ugly hack to remove the entity from the execute of the VRE.
        vre.deleteFromIndex(type, id);
      } catch (IndexException e) {
        LOG.error("Delete from execute went wrong.", e);
      }

      // FIXME: Quick hack to execute and persist the updated relations.
      // TODO: Find a better way to do this.
      for (String relationId : updatedRelationIds) {
        changeHelper.notifyChange(ActionType.MOD, Relation.class, repository.getEntityOrDefaultVariation(Relation.class, relationId), relationId);
      }

    } catch (NoSuchEntityException e) {
      return returnNotFoundResponse(id);
    } catch (StorageException e) {
      LOG.error("Storage exception occured.", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

    return Response.noContent().build();

  }

  protected Response returnNotFoundResponse(String id) {
    return Response.status(Status.NOT_FOUND).entity("Entity with id \"" + id + "\" cannot be found.").build();
  }
}
