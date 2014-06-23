package nl.knaw.huygens.timbuctoo.search.converters;

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.parameters.SortDirection;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

import com.google.common.collect.Lists;

public class SortConverter implements SearchParametersFieldConveter {

  @Override
  public void addToV1(SearchParameters searchParameters, SearchParametersV1 searchParametersV1) {
    List<SortParameter> sortParameters = createSortParamterList();

    sortParameters.add(new SortParameter(searchParameters.getSort(), getSortDirection(searchParameters)));
    searchParametersV1.setSortParameters(sortParameters);
  }

  private SortDirection getSortDirection(SearchParameters searchParameters) {
    return searchParameters.isAscending() ? SortDirection.ASCENDING : SortDirection.DESCENDING;
  }

  protected List<SortParameter> createSortParamterList() {
    return Lists.newArrayList();
  }

}
