package nl.knaw.huygens.timbuctoo.server.rest;

import java.util.List;

public class SearchResponseV2_1Factory {

  public SearchResponseV2_1 createResponse(SearchResult searchResult,
                                           int rows,
                                           int start) {
    SearchResponseV2_1 searchResponse = new SearchResponseV2_1();
    searchResponse.setFullTextSearchFields(searchResult.getFullTextSearchFields());
    searchResponse.setSortableFields(searchResult.getSortableFields());
    searchResponse.setStart(start);

    List<EntityRef> refs = searchResult.getRefs();
    int numFound = refs.size();
    int normalizedStart = mapToRange(start, 0, numFound);
    int normalizedRows = mapToRange(rows, 0, numFound - normalizedStart);
    int end = normalizedStart + normalizedRows;
    searchResponse.setRefs(refs.subList(normalizedStart, end));

    return searchResponse;
  }

  /**
   * Make sure {@code value} is between {@code minValue} and {@code maxValue}.
   *
   * @param value    the value that has to be in the range
   * @param minValue the minimum value of the range
   * @param maxValue the maximum value of the range
   * @return {@code value} if it's in the range,
   * {@code minValue} if {@code value} is lower than the {@code minValue},
   * {@code maxValue} if {@code value} is higher than the {@code maxValue}
   */
  private static int mapToRange(int value, int minValue, int maxValue) {
    return Math.min(Math.max(value, minValue), maxValue);
  }
}
