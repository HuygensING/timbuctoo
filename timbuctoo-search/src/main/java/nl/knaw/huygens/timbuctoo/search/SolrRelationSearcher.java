package nl.knaw.huygens.timbuctoo.search;

import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.SearchException;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

public class SolrRelationSearcher extends RelationSearcher {

  public SolrRelationSearcher(Repository repository, VREManager vreManager) {
    super(repository);
  }

  @Override
  public SearchResult search(VRE vre, RelationSearchParameters relationSearchParameters) throws SearchException {
    // TODO Auto-generated method stub
    return null;
  }

}
