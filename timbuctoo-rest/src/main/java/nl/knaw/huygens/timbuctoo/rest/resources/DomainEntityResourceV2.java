package nl.knaw.huygens.timbuctoo.rest.resources;

import static nl.knaw.huygens.timbuctoo.config.Paths.DOMAIN_PREFIX;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PARAM;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.ID_PARAM;
import static nl.knaw.huygens.timbuctoo.config.Paths.ID_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.PID_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.V2_PATH;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.REVISION_KEY;
import static nl.knaw.huygens.timbuctoo.rest.util.QueryParameters.USER_ID_KEY;
import static nl.knaw.huygens.timbuctoo.security.UserRoles.ADMIN_ROLE;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

@Path(V2_PATH + "/" + DOMAIN_PREFIX + "/" + ENTITY_PATH)
public class DomainEntityResourceV2 extends DomainEntityResource {

  @Inject
  public DomainEntityResourceV2(TypeRegistry registry, Repository repository, Broker broker) {
    super(registry, repository, broker);
  }

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

    return Response.ok(repository.getEntityWithRelations(typeRegistry.getTypeForXName(entityName), id)).build();
  }

  @Override
  @PUT
  @Path(PID_PATH)
  @RolesAllowed(ADMIN_ROLE)
  @Consumes(MediaType.APPLICATION_JSON)
  public void putPIDs(//
      @PathParam(ENTITY_PARAM) String entityName,//
      @HeaderParam(VRE_ID_KEY) String vreId) {
    super.putPIDs(entityName, vreId);
  }

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

    DomainEntity entity = repository.getEntity(type, id);

    if (entity == null) {
      return returnNotFoundResponse(id);
    }

    try {
      repository.deleteDomainEntity(entity);
      notifyChange(ActionType.MOD, type, entity, id);
    } catch (NoSuchEntityException e) {
      return returnNotFoundResponse(id);
    } catch (StorageException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
    }

    return Response.noContent().build();

  }

  protected Response returnNotFoundResponse(String id) {
    return Response.status(Status.NOT_FOUND).entity("Entity with id \"" + id + "\" cannot be found.").build();
  }
}
