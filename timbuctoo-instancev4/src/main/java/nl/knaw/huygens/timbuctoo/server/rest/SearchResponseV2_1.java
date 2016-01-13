package nl.knaw.huygens.timbuctoo.server.rest;

import com.google.common.collect.Lists;

import java.util.List;

class SearchResponseV2_1 {
  private List<Facet> facets;
  private List<String> fullTextSearchFields;
  private List<Ref> refs;
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

  public List<Ref> getRefs() {
    return refs;
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

  public static SearchResponseV2_1 from(WwPersonSearchDescription description) {
    SearchResponseV2_1 searchResponseV2_1 = new SearchResponseV2_1();
    searchResponseV2_1.setFullTextSearchFields(description.getFullTextSearchFields());
    searchResponseV2_1.setSortableFields(description.getSortableFields());

    return searchResponseV2_1;
  }

  private static class Facet {
  }

  private static class Ref {
  }
}
