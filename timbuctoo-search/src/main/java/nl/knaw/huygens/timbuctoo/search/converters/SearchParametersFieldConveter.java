package nl.knaw.huygens.timbuctoo.search.converters;

import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

public interface SearchParametersFieldConveter {

  void addToV1(SearchParameters searchParameters, SearchParametersV1 searchParametersV1);

}