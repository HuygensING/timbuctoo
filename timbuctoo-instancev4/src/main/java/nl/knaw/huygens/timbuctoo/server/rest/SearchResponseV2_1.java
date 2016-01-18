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

  public void setRefs(List<EntityRef> refs) {
    this.refs = refs;
    this.rows = refs.size();
  }

  public List<String> getSortableFields() {
    return sortableFields;
  }

  public void setSortableFields(List<String> sortableFields) {
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

  public void setFullTextSearchFields(List<String> fullTextSearchFields) {
    this.fullTextSearchFields = fullTextSearchFields;
  }



  public void setStart(int start) {
    this.start = start;
  }


  private static class Facet {
  }

  static class SearchResponseRef{

    private String type;
    private String id;

    public String getType() {
      return type;
    }

    public String getId() {
      return id;
    }
  }

}
