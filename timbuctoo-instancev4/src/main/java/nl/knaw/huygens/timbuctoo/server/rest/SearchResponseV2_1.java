package nl.knaw.huygens.timbuctoo.server.rest;

import com.google.common.collect.Lists;

import java.util.List;

class SearchResponseV2_1 {
  private List<Facet> facets;
  private List<String> fullTextSearchFields;
  private List<EntityRef> refs;
  private List<String> sortableFields;
  private int start;
  private int rows;
  private int numFound;

  public SearchResponseV2_1() {
    facets = Lists.newArrayList();
    fullTextSearchFields = Lists.newArrayList();
    refs = Lists.newArrayList();
    sortableFields = Lists.newArrayList();
  }

  public List<Facet> getFacets() {
    return facets;
  }

  public List<String> getFullTextSearchFields() {
    return fullTextSearchFields;
  }

  public List<EntityRef> getRefs() {
    return refs;
  }

  private void setRefs(List<EntityRef> refs) {
    this.refs = refs;
    this.rows = refs.size();
  }

  public List<String> getSortableFields() {
    return sortableFields;
  }

  private void setSortableFields(List<String> sortableFields) {
    this.sortableFields = sortableFields;
  }

  public int getStart() {
    return start;
  }

  public int getRows() {
    return rows;
  }

  public int getNumFound() {
    return numFound;
  }

  private void setFullTextSearchFields(List<String> fullTextSearchFields) {
    this.fullTextSearchFields = fullTextSearchFields;
  }

  public static SearchResponseV2_1 from(SearchResult searchResult,
                                        int rows,
                                        int start) {
    SearchResponseV2_1 searchResponse = new SearchResponseV2_1();
    searchResponse.setFullTextSearchFields(searchResult.getFullTextSearchFields());
    searchResponse.setSortableFields(searchResult.getSortableFields());
    searchResponse.start = start;

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
   * @param value the value that has to be in the range
   * @param minValue the minimum value of the range
   * @param maxValue the maximum value of the range
   * @return {@code value} if it's in the range,
   * {@code minValue} if {@code value} is lower than the {@code minValue},
   * {@code maxValue} if {@code value} is higher than the {@code maxValue}
   */
  private static int mapToRange(int value, int minValue, int maxValue) {
    return Math.min(Math.max(value, minValue), maxValue);
  }


  private static class Facet {
  }

}
