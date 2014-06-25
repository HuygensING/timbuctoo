package nl.knaw.huygens.timbuctoo.rest.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.TimbuctooException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Inject;

public class SearchRequestValidator {

  private final VREManager vreManager;
  private final TypeRegistry typeRegistry;
  private final Repository repository;

  @Inject
  public SearchRequestValidator(VREManager vreManager, TypeRegistry typeRegistry, Repository repository) {
    this.vreManager = vreManager;
    this.typeRegistry = typeRegistry;
    this.repository = repository;
  }

  public void validate(String vreId, SearchParametersV1 searchParameters) throws TimbuctooException {
    VRE vre = isValidVRE(vreId);

    isValidType(vre, searchParameters.getTypeString());

    isValidTerm(searchParameters);
  }

  protected void isValidTerm(SearchParametersV1 searchParameters) {
    checkNotNull(searchParameters.getTerm(), BAD_REQUEST, "No query parameter specified");
  }

  protected void isValidType(VRE vre, String typeString) {
    checkNotNull(StringUtils.trimToNull(typeString), BAD_REQUEST, "No 'typeString' parameter specified");
    Class<? extends DomainEntity> type = typeRegistry.getDomainEntityType(typeString);
    checkNotNull(type, BAD_REQUEST, "No domain entity type for \"%s\"", typeString);

    checkCondition(vre.inScope(type), BAD_REQUEST, "Type not in scope: \"%s\"", typeString);
  }

  protected VRE isValidVRE(String vreId) {
    checkNotNull(vreId, BAD_REQUEST, "No VRE id specified");
    VRE vre = vreManager.getVREById(vreId);
    checkNotNull(vre, BAD_REQUEST, "No VRE with id \"%s\"", vreId);
    return vre;
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

  /**
   * Validates the VRE and the RelationSearchParamters 
   * @param vreId
   * @param relationSearchParameters
   * @throws TimbuctooException when one of the parameters is invalid.
   */
  public void validateRelationRequest(String vreId, RelationSearchParameters relationSearchParameters) {
    VRE vre = isValidVRE(vreId);
    isValidRelationType(vre, relationSearchParameters.getTypeString());
    isValidSearch(relationSearchParameters.getSourceSearchId());
    isValidSearch(relationSearchParameters.getTargetSearchId());

    areValidRelationTypes(relationSearchParameters.getRelationTypeIds());
  }

  private void isValidRelationType(VRE vre, String typeString) {
    checkNotNull(StringUtils.trimToNull(typeString), BAD_REQUEST, "No 'typeString' parameter specified");
    Class<? extends DomainEntity> type = typeRegistry.getDomainEntityType(typeString);
    checkNotNull(type, BAD_REQUEST, "No domain entity type for \"%s\"", typeString);

    checkCondition(vre.inScope(type), BAD_REQUEST, "Type not in scope: \"%s\"", typeString);
    checkCondition(Relation.class.isAssignableFrom(type), BAD_REQUEST, "Not a relation type: %s", typeString);
  }

  private void areValidRelationTypes(List<String> relationTypeIds) {
    for (String relationTypeId : relationTypeIds) {
      checkNotNull(repository.getRelationTypeById(relationTypeId), BAD_REQUEST, "Relation type with id %s does not exist", relationTypeId);
    }
  }

  private void isValidSearch(String searchId) {
    checkNotNull(searchId, BAD_REQUEST, "sourceSearchId is not specified");
    checkNotNull(repository.getEntity(SearchResult.class, searchId), BAD_REQUEST, "Search result for id %s does not exist.", searchId);
  }
}
