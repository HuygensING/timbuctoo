package test.model.projecta;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import test.model.BaseDomainEntity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubADomainEntity extends BaseDomainEntity {

  public static final String VALUEA2_NAME = "valuea";
  @JsonProperty(VALUEA2_NAME)
  private String valuea;

  public static final String VALUEA3_NAME = "valuea3";
  @JsonProperty(VALUEA3_NAME)
  private String valuea3;

  public SubADomainEntity() {}

  public SubADomainEntity(String id) {
    setId(id);
  }

  public SubADomainEntity(String id, String pid) {
    setId(id);
    setPid(pid);
  }

  public SubADomainEntity(String id, String pid, String value1, String value2, String valuea) {
    setId(id);
    setPid(pid);
    setValue1(value1);
    setValue2(value2);
    setValuea(valuea);
  }

  public String getValuea() {
    return valuea;
  }

  public void setValuea(String valuea) {
    this.valuea = valuea;
  }

  public String getValuea3() {
    return valuea3;
  }

  public void setValuea3(String valuea3) {
    this.valuea3 = valuea3;
  }

}
