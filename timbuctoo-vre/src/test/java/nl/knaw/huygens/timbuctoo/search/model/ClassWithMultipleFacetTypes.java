package nl.knaw.huygens.timbuctoo.search.model;

import nl.knaw.huygens.facetedsearch.model.FacetType;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class ClassWithMultipleFacetTypes extends DomainEntity {
  private String test;
  private boolean bool;
  private int integer;
  private String period;

  @Override
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  @IndexAnnotation(fieldName = "dynamic_s_list", facetType = FacetType.LIST, isFaceted = true)
  public String getTest() {
    return test;
  }

  public void setTest(String test) {
    this.test = test;
  }

  @IndexAnnotation(fieldName = "dynamic_b_boolean", facetType = FacetType.BOOLEAN, isFaceted = true)
  public boolean isBool() {
    return bool;
  }

  public void setBool(boolean bool) {
    this.bool = bool;
  }

  @IndexAnnotation(fieldName = "dynamic_r_range", facetType = FacetType.RANGE, isFaceted = true)
  public int getInteger() {
    return integer;
  }

  public void setInteger(int integer) {
    this.integer = integer;
  }

  @IndexAnnotation(fieldName = "dynamic_p_period", facetType = FacetType.PERIOD, isFaceted = true)
  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

}
