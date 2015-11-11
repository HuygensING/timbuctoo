package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.facetedsearch.model.Facet;

import java.util.List;
import java.util.Set;

public class RelationSearchResultDTOV2_1 extends SearchResultDTO implements RelationSearchable{

  private String term;
  private List<Facet> facets;
  private List<RelationDTO> refs;
  private Set<String> fullTextSearchFields;

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public List<Facet> getFacets() {
    return facets;
  }

  public void setFacets(List<Facet> facets) {
    this.facets = facets;
  }

  @Override
  public List<RelationDTO> getRefs() {
    return refs;
  }

  public void setRefs(List<RelationDTO> refs) {
    this.refs = refs;
  }

  public Set<String> getFullTextSearchFields() {
    return fullTextSearchFields;
  }

  public void setFullTextSearchFields(Set<String> fields) {
    this.fullTextSearchFields = fields;
  }
}
