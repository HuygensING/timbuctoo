package nl.knaw.huygens.timbuctoo.rest.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.security.client.filters.AbstractRolesAllowedResourceFilterFactory;
import nl.knaw.huygens.security.client.filters.BypassFilter;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders;
import nl.knaw.huygens.timbuctoo.vre.Scope;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

public class VREAuthorizationFilterFactory extends AbstractRolesAllowedResourceFilterFactory {

  private final VREManager vreManager;
  private final TypeRegistry typeRegistry;

  @Inject
  public VREAuthorizationFilterFactory(VREManager vreManager, TypeRegistry typeRegistry) {
    this.vreManager = vreManager;
    this.typeRegistry = typeRegistry;
  }

  @Override
  protected ResourceFilter createResourceFilter(AbstractMethod am) {
    return new VREAuthorizationResourceFilter(this.vreManager, this.typeRegistry);
  }

  @Override
  protected ResourceFilter createNoSecurityResourceFilter() {
    return new BypassFilter();
  }

  /**
   * This class filters the requests to see if the user is logged in into a valid VRE.
   */
  protected static class VREAuthorizationResourceFilter implements ResourceFilter, ContainerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(VREAuthorizationResourceFilter.class);
    private static final String ITEM_REGEX = String.format("%s/(%s)/(%s)", Paths.DOMAIN_PREFIX, Paths.ENTITY_REGEX, Paths.ID_REGEX);
    private static final String COLLECTION_REGEX = String.format("%s/(%s)", Paths.DOMAIN_PREFIX, Paths.ENTITY_REGEX);
    private final VREManager vreManager;
    private final TypeRegistry typeRegistry;

    public VREAuthorizationResourceFilter(VREManager vreManager, TypeRegistry typeRegistry) {
      this.vreManager = vreManager;
      this.typeRegistry = typeRegistry;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
      // Allow every registered user to request their own information.
      //      if ("GET".equals(request.getMethod()) && request.getPath().contains("/users/me")) {
      //        return request;
      //      }
      // Get the VRE.
      String vreId = request.getHeaderValue(CustomHeaders.VRE_ID_KEY);

      if (vreId == null) {
        LOG.error("No VRE id was send with the request.");
        throw new WebApplicationException(Status.UNAUTHORIZED);
      }

      VRE vre = vreManager.getVREById(vreId);

      if (vre == null) {
        LOG.error("VRE with id {} is not known.", vreId);
        throw new WebApplicationException(Status.FORBIDDEN);
      }

      Scope scope = vre.getScope();

      String path = request.getPath();

      if (path.matches(ITEM_REGEX)) {
        Pattern pattern = Pattern.compile(ITEM_REGEX);
        Matcher matcher = pattern.matcher(path);

        String typeString = null;
        String id = null;

        if (matcher.find()) {
          typeString = matcher.group(1);
          id = matcher.group(2);
        }

        Class<? extends Entity> type = typeRegistry.getTypeForXName(typeString);

        if (TypeRegistry.isDomainEntity(type) && !scope.inScope(TypeRegistry.toDomainEntity(type), id)) {
          throw new WebApplicationException(Status.FORBIDDEN);
        }
      } else if (path.matches(COLLECTION_REGEX)) {
        Pattern pattern = Pattern.compile(COLLECTION_REGEX);
        Matcher matcher = pattern.matcher(path);

        String typeString = null;

        if (matcher.find()) {
          typeString = matcher.group(1);
        }

        Class<? extends Entity> type = typeRegistry.getTypeForXName(typeString);
        if (TypeRegistry.isDomainEntity(type) && !scope.isTypeInScope(TypeRegistry.toDomainEntity(type))) {
          throw new WebApplicationException(Status.FORBIDDEN);
        }
      }

      // kijk of de entity binnen de scope van de VRE valt.
      // Haal de entity naam uit het pad.
      // Haal het type op
      // kijk of het pad een id bevat.

      return request;
    }

    @Override
    public ContainerRequestFilter getRequestFilter() {
      return this;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
      return null;
    }

  }

}
