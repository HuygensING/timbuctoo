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

  public static SearchResponseV2_1 from(WwPersonSearchDescription description,
                                        SearchResult searchResult,
                                        int rows,
                                        int start) {
    SearchResponseV2_1 searchResponse = new SearchResponseV2_1();
    searchResponse.setFullTextSearchFields(description.getFullTextSearchFields());
    searchResponse.setSortableFields(description.getSortableFields());
    List<EntityRef> refs = searchResult.getRefs();
    searchResponse.setRefs(refs.subList(start, Math.min(start + rows, refs.size())));

    return searchResponse;
  }


  private static class Facet {
  }

}
