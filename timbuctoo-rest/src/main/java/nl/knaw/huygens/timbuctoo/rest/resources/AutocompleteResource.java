package nl.knaw.huygens.timbuctoo.rest.resources;

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.rest.util.AutocompleteResultConverter;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static nl.knaw.huygens.timbuctoo.config.Paths.AUTOCOMPLETE_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.DOMAIN_PREFIX;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PARAM;
import static nl.knaw.huygens.timbuctoo.config.Paths.ENTITY_PATH;
import static nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders.VRE_ID_KEY;

@Path(Paths.V2_PATH + "/" + DOMAIN_PREFIX + "/" + ENTITY_PATH + "/" + AUTOCOMPLETE_PATH)
public class AutocompleteResource extends ResourceBase{
  private final TypeRegistry typeRegistry;
  private final AutocompleteResultConverter resultConverter;

  @Inject
  public AutocompleteResource(Repository repository, VRECollection vreCollection, TypeRegistry typeRegistry, AutocompleteResultConverter resultConverter) {
    super(repository, vreCollection);
    this.typeRegistry = typeRegistry;
    this.resultConverter = resultConverter;
  }

  @GET
  @Produces(APPLICATION_JSON)
  public Response get(@PathParam(ENTITY_PARAM) String entityName, @QueryParam("query") String query, //
                      @HeaderParam(VRE_ID_KEY) String vreId){
    Class<? extends DomainEntity> type = getValidEntityType(entityName);
    VRE vre = getValidVRE(vreId);

    Iterable<Map<String, Object>> rawSearchResult = vre.doRawSearch(type, query);
    Iterable<Map<String, Object>> result = resultConverter.convert(rawSearchResult);

    return Response.ok(result).build();
  }

  protected final Class<? extends DomainEntity> getValidEntityType(String name) {
    return checkNotNull(typeRegistry.getTypeForXName(name), NOT_FOUND, "No domain entity collection %s", name);
  }
}
