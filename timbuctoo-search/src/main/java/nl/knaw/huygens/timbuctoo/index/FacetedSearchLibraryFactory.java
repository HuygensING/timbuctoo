package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.facetedsearch.FacetedSearchLibrary;
import nl.knaw.huygens.facetedsearch.definition.SolrSearcher;

public class FacetedSearchLibraryFactory {

  public FacetedSearchLibrary create(SolrSearcher solrSearcher) {
    return new FacetedSearchLibrary(solrSearcher);
  }

}
