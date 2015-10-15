package nl.knaw.huygens.timbuctoo.vre;

import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;

public class RelationSearchParametersV2_1 extends FacetedSearchParameters<RelationSearchParametersV2_1> {

  private String otherSearchId;

  public String getOtherSearchId() {
    return otherSearchId;
  }

  public RelationSearchParametersV2_1 setOtherSearchId(String otherSearchId) {
    this.otherSearchId = otherSearchId;
    return this;
  }
}
