package nl.knaw.huygens.timbuctoo.rest.model;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@IDPrefix("TSYS")
public class TestSystemEntity extends SystemEntity {

  private String name;
  private String testValue1;
  private String testValue2;
  @JsonProperty("propAnnotated")
  private String annotatedProperty;
  private String propWithAnnotatedAccessors;

  public TestSystemEntity() {}

  public TestSystemEntity(String id) {
    setId(id);
  }

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTestValue1() {
    return testValue1;
  }

  public void setTestValue1(String testValue1) {
    this.testValue1 = testValue1;
  }

  public String getTestValue2() {
    return testValue2;
  }

  public void setTestValue2(String testValue2) {
    this.testValue2 = testValue2;
  }

  public String getAnnotatedProperty() {
    return annotatedProperty;
  }

  public void setAnnotatedProperty(String annotatedProperty) {
    this.annotatedProperty = annotatedProperty;
  }

  @JsonProperty("pwaa")
  public String getPropWithAnnotatedAccessors() {
    return propWithAnnotatedAccessors;
  }

  @JsonProperty("pwaa")
  public void setPropWithAnnotatedAccessors(String propWithAnnotatedAccessors) {
    this.propWithAnnotatedAccessors = propWithAnnotatedAccessors;
  }

}
