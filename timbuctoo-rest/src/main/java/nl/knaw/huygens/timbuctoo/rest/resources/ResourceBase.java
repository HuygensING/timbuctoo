package nl.knaw.huygens.timbuctoo.rest.resources;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

/**
 * Base class for Timbuctoo resources.
 */
public class ResourceBase {

  /**
   * Checks the specified reference and throws a {@code WebApplicationException}
   * with the specified status if the reference is {@code null}.
   */
  protected <T> T checkNotNull(T reference, Status status) {
    if (reference == null) {
      throw new WebApplicationException(status);
    }
    return reference;
  }

}
