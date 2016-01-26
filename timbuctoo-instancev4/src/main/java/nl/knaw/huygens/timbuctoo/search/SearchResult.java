package nl.knaw.huygens.timbuctoo.search;

import nl.knaw.huygens.timbuctoo.search.description.facet.Facet;

import java.util.List;

public class SearchResult {

  private List<EntityRef> refs;
  private List<String> fullTextSearchFields;
  private List<String> sortableFields;

  public SearchResult(List<EntityRef> refs, List<String> fullTextSearchFields, List<String> sortableFields,
                      List<Facet> facets) {
    this.refs = refs;
    this.fullTextSearchFields = fullTextSearchFields;
    this.sortableFields = sortableFields;
  }

  public SearchResult(List<EntityRef> refs, SearchDescription description, List<Facet> facets) {
    this(refs, description.getFullTextSearchFields(), description.getSortableFields(), facets);
  }

  public List<EntityRef> getRefs() {
    return refs;
  }

  public List<String> getFullTextSearchFields() {
    return fullTextSearchFields;
  }

  public List<String> getSortableFields() {
    return sortableFields;
  }
}
