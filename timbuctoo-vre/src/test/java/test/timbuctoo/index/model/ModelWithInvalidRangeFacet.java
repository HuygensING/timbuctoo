package test.timbuctoo.index.model;

import nl.knaw.huygens.facetedsearch.model.FacetType;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class ModelWithInvalidRangeFacet extends DomainEntity {
  private String invalidRangeFacet;

  @Override
  public String getDisplayName() {
    return null;
  }

  @IndexAnnotation(fieldName = "dynamic_s_test", facetType = FacetType.RANGE, isFaceted = true)
  public String getInvalidRangeFacet() {
    return invalidRangeFacet;
  }

  public void setInvalidRangeFacet(String invalidRangeFacet) {
    this.invalidRangeFacet = invalidRangeFacet;
  }

}
