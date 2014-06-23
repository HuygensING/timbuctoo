package nl.knaw.huygens.timbuctoo.search.converters;

import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

public class SearchParametersConverter {

  private final SearchParametersFieldConveter[] converters;

  public SearchParametersConverter(SearchParametersFieldConveter... converters) {
    this.converters = converters;

  }

  public SearchParametersV1 toV1(SearchParameters searchParameters) {
    SearchParametersV1 searchParametersV1 = createParametersV1();
    searchParametersV1.setTerm(searchParameters.getTerm());
    searchParametersV1.setFuzzy(searchParameters.isFuzzy());
    searchParametersV1.setTypeString(searchParameters.getTypeString());
    for (SearchParametersFieldConveter converter : converters) {
      converter.addToV1(searchParameters, searchParametersV1);
    }
    return searchParametersV1;
  }

  protected SearchParametersV1 createParametersV1() {
    return new SearchParametersV1();
  }

}
