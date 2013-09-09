package nl.knaw.huygens.solr;

import nl.knaw.huygens.repository.facet.annotations.FacetType;

public class FacetInfo {
  String name = "";
  String title = "";
  FacetType type = FacetType.LIST;

  public String getName() {
    return name;
  }

  public FacetInfo setName(String name) {
    this.name = name;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public FacetInfo setTitle(String title) {
    this.title = title;
    return this;
  }

  public FacetType getType() {
    return type;
  }

  public FacetInfo setType(FacetType type) {
    this.type = type;
    return this;
  }

}
