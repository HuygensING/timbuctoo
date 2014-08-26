package nl.knaw.huygens.timbuctoo.search.model;

/*
 * #%L
 * Timbuctoo VRE
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import nl.knaw.huygens.facetedsearch.model.FacetType;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

public class ClassWithMultipleFacetTypes extends DomainEntity {
  private String test;
  private boolean bool;
  private Datable datable;
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

  @IndexAnnotation(fieldName = "dynamic_d_range", facetType = FacetType.RANGE, isFaceted = true)
  public Datable getDatable() {
    return datable;
  }

  public void setDatable(Datable datable) {
    this.datable = datable;
  }

  @IndexAnnotation(fieldName = "dynamic_p_period", facetType = FacetType.PERIOD, isFaceted = true)
  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

}
