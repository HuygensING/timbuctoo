package nl.knaw.huygens.timbuctoo.rest.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import com.google.inject.Inject;

public class SearchRequestValidator {

  private final VREManager vreManagerMock;
  private final TypeRegistry typeRegistryMock;

  @Inject
  public SearchRequestValidator(VREManager vreManagerMock, TypeRegistry typeRegistryMock) {
    this.vreManagerMock = vreManagerMock;
    this.typeRegistryMock = typeRegistryMock;
  }

  public void validate(String vreId, SearchParameters searchParameters) throws TimbuctooException {
    checkNotNull(vreId, BAD_REQUEST, "No VRE id specified");
    VRE vre = vreManagerMock.getVREById(vreId);
    checkNotNull(vre, BAD_REQUEST, "No VRE with id \"%s\"", vreId);

    String typeString = searchParameters.getTypeString();
    checkNotNull(typeString, BAD_REQUEST, "No 'typeString' parameter specified");
    Class<? extends DomainEntity> type = typeRegistryMock.getDomainEntityType(typeString);
    checkNotNull(type, BAD_REQUEST, "No domain entity type for \"%s\"", typeString);

    checkCondition(vre.inScope(type), BAD_REQUEST, "Type not in scope: \"%s\"", typeString);

    checkNotNull(searchParameters.getTerm(), BAD_REQUEST, "No query parameter specified");

  }

  /**
   * Checks the specified condition
   * and throws a {@code TimbuctooException} if the condition is {@code false}.
   */
  protected void checkCondition(boolean condition, Status status, String errorMessageTemplate, Object... errorMessageArgs) {
    if (!condition) {
      throw new TimbuctooException(status, errorMessageTemplate, errorMessageArgs);
    }
  }

  /**
   * Checks the specified reference
   * and throws a {@code TimbuctooException} if the reference is {@code null}.
   */
  protected <T> void checkNotNull(T reference, Status status, String errorMessageTemplate, Object... errorMessageArgs) {
    checkCondition(reference != null, status, errorMessageTemplate, errorMessageArgs);
  }

}
