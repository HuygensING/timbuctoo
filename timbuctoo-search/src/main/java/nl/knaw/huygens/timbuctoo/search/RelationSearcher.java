package nl.knaw.huygens.timbuctoo.search;

import java.util.List;

import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RelationSearcher {

  private static final Logger LOG = LoggerFactory.getLogger(RelationSearcher.class);
  protected final Repository repository;

  public RelationSearcher(Repository repository) {
    this.repository = repository;
  }

  public abstract SearchResult search(VRE vre, Class<? extends DomainEntity> relationType, RelationSearchParameters relationSearchParameters) throws SearchException, SearchValidationException;

  protected void logStopWatchTimeInSeconds(StopWatch stopWatch, String eventDescription) {
    LOG.info(String.format("%s: %.3f seconds", eventDescription, (double) stopWatch.getTime() / 1000));
  }

  protected List<String> getRelationTypes(List<String> relationTypeIds, VRE vre) {
    if (relationTypeIds != null && !relationTypeIds.isEmpty()) {
      return relationTypeIds;
    }

    // TODO find a more generic way, to retrieve the relation ids of a VRE.
    return repository.getRelationTypeIdsByName(vre.getReceptionNames());
  }

}