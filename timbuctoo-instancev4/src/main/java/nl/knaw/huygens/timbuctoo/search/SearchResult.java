package nl.knaw.huygens.timbuctoo.search;

import java.util.List;

public class SearchResult {

  private List<EntityRef> refs;
  private List<String> fullTextSearchFields;
  private List<String> sortableFields;

  public SearchResult(List<EntityRef> refs, List<String> fullTextSearchFields, List<String> sortableFields) {
    this.refs = refs;
    this.fullTextSearchFields = fullTextSearchFields;
    this.sortableFields = sortableFields;
  }

  public SearchResult(List<EntityRef> refs, WwPersonSearchDescription description) {
    this(refs, description.getFullTextSearchFields(), description.getSortableFields());
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
