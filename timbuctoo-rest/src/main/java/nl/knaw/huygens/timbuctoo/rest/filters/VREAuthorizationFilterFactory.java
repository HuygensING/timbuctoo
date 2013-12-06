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
import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
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
  private final StorageManager storageManager;

  @Inject
  public VREAuthorizationFilterFactory(VREManager vreManager, TypeRegistry typeRegistry, StorageManager storageManager) {
    this.vreManager = vreManager;
    this.typeRegistry = typeRegistry;
    this.storageManager = storageManager;
  }

  @Override
  protected ResourceFilter createResourceFilter(AbstractMethod am) {
    return new VREAuthorizationResourceFilter(this.vreManager, this.typeRegistry, this.storageManager);
  }

  @Override
  protected ResourceFilter createNoSecurityResourceFilter() {
    return new BypassFilter();
  }

  /**
   * This class filters the requests to see if the user is logged in into a valid {@code VRE}, 
   * and if the requested data is in the {@code Scope} of the {@code VRE}.
   * This filter is only used for protected resources.
   */
  protected static class VREAuthorizationResourceFilter implements ResourceFilter, ContainerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(VREAuthorizationResourceFilter.class);
    private static final String ITEM_REGEX = String.format("%s/(%s)/(%s)", Paths.DOMAIN_PREFIX, Paths.ENTITY_REGEX, Paths.ID_REGEX);
    private static final String COLLECTION_REGEX = String.format("%s/(%s)", Paths.DOMAIN_PREFIX, Paths.ENTITY_REGEX);
    private final VREManager vreManager;
    private final TypeRegistry typeRegistry;
    private final StorageManager storageManager;

    public VREAuthorizationResourceFilter(VREManager vreManager, TypeRegistry typeRegistry, StorageManager storageManager) {
      this.vreManager = vreManager;
      this.typeRegistry = typeRegistry;
      this.storageManager = storageManager;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
      // Get the requested path
      String path = request.getPath();

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

      if (!isAllowedToShowSystemEntity(path, vreId) && !isDomainEntityInScope(path, vre.getScope())) {
        throw new WebApplicationException(Status.FORBIDDEN);
      }

      return request;
    }

    private boolean isDomainEntityInScope(String path, Scope scope) {
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
          return false;
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
          return false;
        }
      }
      return true;
    }

    private boolean isAllowedToShowSystemEntity(String path, String vreId) {
      return path.contains(Paths.SYSTEM_PREFIX) && isUserInScope(path, vreId);
    }

    private boolean isUserInScope(String path, String vreId) {

      String userItemRegex = String.format("%s/%s/(%s)", Paths.SYSTEM_PREFIX, Paths.USER_PATH, Paths.ID_REGEX);
      if (path.matches(userItemRegex)) {
        Pattern pattern = Pattern.compile(userItemRegex);
        Matcher matcher = pattern.matcher(path);

        String userId = null;
        if (matcher.find()) {
          userId = matcher.group(1);
        }

        VREAuthorization example = new VREAuthorization();
        example.setUserId(userId);
        example.setVreId(vreId);

        return this.storageManager.findEntity(VREAuthorization.class, example) != null;

      }

      return path.contains(Paths.USER_PATH);
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
